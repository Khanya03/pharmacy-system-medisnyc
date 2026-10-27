package za.ac.cput.medisnyc.service;

/* OrderService.java
   Module 4/6: Order fulfilment - turns a set of requested medications into a
   dispensed order, decrementing inventory stock and computing revenue.
   Mirrors the Doctor/Patient service pattern for ID generation.
*/

import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderItem;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionItem;
import za.ac.cput.medisnyc.exception.InsufficientStockException;
import za.ac.cput.medisnyc.factory.OrderFactory;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;

@Service
public class OrderService implements PrescriptionOrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final MedicationJpaRepository medicationRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public OrderService(OrderJpaRepository orderRepository,
                        OrderItemJpaRepository orderItemRepository,
                        InventoryJpaRepository inventoryRepository,
                        MedicationJpaRepository medicationRepository,
                        AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.medicationRepository = medicationRepository;
        this.auditLogService = auditLogService;
    }

    public List<Order> getAll() {
        return attachItems(orderRepository.findAll());
    }

    public List<Order> getByPatient(String patientId) {
        return attachItems(orderRepository.findByPatientId(patientId));
    }

    public Order getById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return attachItems(List.of(order)).get(0);
    }

    public Order getByPrescriptionId(String prescriptionId) {
        return orderRepository.findFirstByPrescriptionId(prescriptionId)
                .map(order -> getById(order.getOrderId()))
                .orElse(null);
    }

    @Transactional
    public void cancelForPrescription(String prescriptionId, String actingUsername) {
        Order order = getByPrescriptionId(prescriptionId);
        if (order != null && (order.getDispensingStatus() == OrderStatus.PENDING
                || order.getDispensingStatus() == OrderStatus.PROCESSING)) {
            updateStatus(order.getOrderId(), OrderStatus.CANCELLED, actingUsername);
        }
    }

    @Transactional
    public Order createForPrescription(Prescription prescription, List<PrescriptionItem> prescriptionItems) {
        Order existingOrder = getByPrescriptionId(prescription.getPrescriptionId());
        if (existingOrder != null) {
            return existingOrder.getDispensingStatus() == OrderStatus.PENDING
                    ? updateStatus(existingOrder.getOrderId(), OrderStatus.PROCESSING, "SYSTEM")
                    : existingOrder;
        }
        if (prescriptionItems == null || prescriptionItems.isEmpty()) {
            throw new IllegalStateException("Prescription has no medication items");
        }

        Map<String, Integer> requestedByMedication = new LinkedHashMap<>();
        for (PrescriptionItem item : prescriptionItems) {
            requestedByMedication.merge(item.getMedicationId(), item.getQuantity(), Integer::sum);
        }

        List<ItemRequest> allocation = new ArrayList<>();
        List<String> unavailable = new ArrayList<>();
        for (Map.Entry<String, Integer> request : requestedByMedication.entrySet()) {
            int remaining = request.getValue();
            List<Inventory> batches = inventoryRepository
                    .findByMedicationIdAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                            request.getKey(), LocalDate.now());
            for (Inventory batch : batches) {
                int allocated = Math.min(remaining, batch.getStockLevel());
                if (allocated > 0) {
                    allocation.add(new ItemRequest(request.getKey(), batch.getInventoryId(), allocated));
                    remaining -= allocated;
                }
                if (remaining == 0) break;
            }
            if (remaining > 0) unavailable.add(request.getKey());
        }
        if (!unavailable.isEmpty()) {
            throw insufficientStock(unavailable);
        }

        Order order = create(prescription.getPatientId(), prescription.getPrescriptionId(), "SYSTEM",
                "Automatically created for prescription " + prescription.getPrescriptionId(), allocation);
        return updateStatus(order.getOrderId(), OrderStatus.PROCESSING, "SYSTEM");
    }

    private List<Order> attachItems(List<Order> orders) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            result.add(new Order.Builder()
                    .setOrderId(order.getOrderId())
                    .setPatientId(order.getPatientId())
                    .setPrescriptionId(order.getPrescriptionId())
                    .setOrderDate(order.getOrderDate())
                    .setDispensingStatus(order.getDispensingStatus())
                    .setTotalAmount(order.getTotalAmount())
                    .setPharmacistId(order.getPharmacistId())
                    .setNotes(order.getNotes())
                    .setItems(items)
                    .build());
        }
        return result;
    }

    /**
     * Request line: what the patient/pharmacist is asking for. inventoryId picks
     * the specific stock batch to dispense from, so price and stock decrement are
     * unambiguous when a medication has more than one batch on hand.
     */
    public record ItemRequest(String medicationId, String inventoryId, int quantity) {}

    @Transactional
    public Order create(String patientId, String prescriptionId, String pharmacistId,
                        String notes, List<ItemRequest> requestedItems) {
        if (Helper.isNullOrEmpty(patientId)) {
            throw new IllegalArgumentException("Patient ID is required");
        }
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item");
        }

        String orderId = Helper.generateId("ORD");
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest req : requestedItems) {
            if (req.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for " + req.medicationId());
            }
            Inventory batch = inventoryRepository.findByIdForUpdate(req.inventoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Stock batch not found: " + req.inventoryId()));
            if (!batch.getMedicationId().equals(req.medicationId())) {
                throw new IllegalArgumentException("Medication does not match stock batch " + req.inventoryId());
            }
            if (!batch.canFulfillOrder(req.quantity())) {
                throw insufficientStock(List.of(batch.getMedicationId()));
            }
            if (inventoryRepository.decrementStockAtomically(
                    req.inventoryId(), req.quantity(), LocalDate.now()) == 0) {
                throw insufficientStock(List.of(batch.getMedicationId()));
            }

            OrderItem item = new OrderItem.Builder()
                    .setOrderItemId(Helper.generateId("ORDI"))
                    .setOrderId(orderId)
                    .setMedicationId(req.medicationId())
                    .setInventoryId(req.inventoryId())
                    .setQuantity(req.quantity())
                    .setPrice(batch.getUnitPrice())
                    .build();
            items.add(item);
            total = total.add(item.getSubtotal());

        }

        Order order = OrderFactory.createOrder(orderId, patientId, prescriptionId, null,
                OrderStatus.PENDING, total, pharmacistId, notes, items);
        Order saved = orderRepository.save(order);
        for (OrderItem item : items) {
            orderItemRepository.save(item);
        }

        auditLogService.log(pharmacistId != null ? pharmacistId : patientId, "CREATE_ORDER", orderId,
                "Order created for patient " + patientId + ", total R" + total);

        return getById(saved.getOrderId());
    }

    private InsufficientStockException insufficientStock(List<String> medicationIds) {
        List<String> medicineNames = medicationIds.stream()
                .map(medicationId -> medicationRepository.findById(medicationId)
                        .map(Medication::getMedicationName)
                        .orElse(medicationId))
                .toList();
        return new InsufficientStockException(medicineNames);
    }

    @Transactional
    public Order updateStatus(String orderId, OrderStatus newStatus, String actingUsername) {
        Order existing = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus current = existing.getDispensingStatus();
        boolean validTransition = switch (current) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.DISPENSED || newStatus == OrderStatus.CANCELLED;
            case DISPENSED -> newStatus == OrderStatus.COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };
        if (!validTransition) {
            throw new IllegalStateException("Order cannot change from " + current + " to " + newStatus);
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : items) {
                Inventory batch = inventoryRepository.findByIdForUpdate(item.getInventoryId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Stock batch no longer exists: " + item.getInventoryId()));
                Inventory restocked = new Inventory.Builder()
                        .setInventoryId(batch.getInventoryId())
                        .setMedicationId(batch.getMedicationId())
                        .setStockLevel(batch.getStockLevel() + item.getQuantity())
                        .setExpiryDate(batch.getExpiryDate())
                        .setBatchNumber(batch.getBatchNumber())
                        .setSupplier(batch.getSupplier())
                        .setUnitPrice(batch.getUnitPrice())
                        .setReceivedDate(batch.getReceivedDate())
                        .setReorderLevel(batch.getReorderLevel())
                        .build();
                inventoryRepository.save(restocked);
            }
        }

        Order updated = new Order.Builder()
                .setOrderId(existing.getOrderId())
                .setPatientId(existing.getPatientId())
                .setPrescriptionId(existing.getPrescriptionId())
                .setOrderDate(existing.getOrderDate())
                .setDispensingStatus(newStatus)
                .setTotalAmount(existing.getTotalAmount())
                .setPharmacistId(existing.getPharmacistId())
                .setNotes(existing.getNotes())
                .setItems(items)
                .build();
        Order saved = orderRepository.save(updated);

        auditLogService.log(actingUsername, "UPDATE_ORDER_STATUS", orderId, "Status changed to " + newStatus);

        return getById(saved.getOrderId());
    }
}
