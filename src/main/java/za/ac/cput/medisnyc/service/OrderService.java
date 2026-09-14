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
import za.ac.cput.medisnyc.factory.OrderFactory;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public OrderService(OrderJpaRepository orderRepository,
                        OrderItemJpaRepository orderItemRepository,
                        InventoryJpaRepository inventoryRepository,
                        AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryRepository = inventoryRepository;
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
            Inventory batch = inventoryRepository.findById(req.inventoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Stock batch not found: " + req.inventoryId()));
            if (batch.getStockLevel() < req.quantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for " + batch.getMedicationId() + " (have " + batch.getStockLevel()
                                + ", need " + req.quantity() + ")");
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

            // Decrement stock now the order has committed to this batch.
            Inventory updatedBatch = new Inventory.Builder()
                    .setInventoryId(batch.getInventoryId())
                    .setMedicationId(batch.getMedicationId())
                    .setStockLevel(batch.getStockLevel() - req.quantity())
                    .setExpiryDate(batch.getExpiryDate())
                    .setBatchNumber(batch.getBatchNumber())
                    .setSupplier(batch.getSupplier())
                    .setUnitPrice(batch.getUnitPrice())
                    .setReceivedDate(batch.getReceivedDate())
                    .setReorderLevel(batch.getReorderLevel())
                    .build();
            inventoryRepository.save(updatedBatch);
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

    @Transactional
    public Order updateStatus(String orderId, OrderStatus newStatus, String actingUsername) {
        Order existing = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Order updated = new Order.Builder()
                .setOrderId(existing.getOrderId())
                .setPatientId(existing.getPatientId())
                .setPrescriptionId(existing.getPrescriptionId())
                .setOrderDate(existing.getOrderDate())
                .setDispensingStatus(newStatus)
                .setTotalAmount(existing.getTotalAmount())
                .setPharmacistId(existing.getPharmacistId())
                .setNotes(existing.getNotes())
                .build();
        Order saved = orderRepository.save(updated);

        auditLogService.log(actingUsername, "UPDATE_ORDER_STATUS", orderId, "Status changed to " + newStatus);

        return getById(saved.getOrderId());
    }
}
