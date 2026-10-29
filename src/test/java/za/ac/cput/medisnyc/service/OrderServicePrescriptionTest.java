package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.OrderPaymentStatus;
import za.ac.cput.medisnyc.domain.OrderItem;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionItem;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.AuditLogJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import za.ac.cput.medisnyc.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

class OrderServicePrescriptionTest {

    @Test
    void createsProcessingOrderUsingEarliestExpiryStockAcrossBatches() {
        Inventory early = batch("INV-EARLY", 3, LocalDate.now().plusDays(10));
        Inventory late = batch("INV-LATE", 5, LocalDate.now().plusDays(20));
        InventoryJpaRepository inventory = mock(InventoryJpaRepository.class);
        when(inventory.findByMedicationIdAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                "MED-1", LocalDate.now())).thenReturn(List.of(early, late));
        when(inventory.findByIdForUpdate("INV-EARLY")).thenReturn(Optional.of(early));
        when(inventory.findByIdForUpdate("INV-LATE")).thenReturn(Optional.of(late));
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        AtomicReference<Order> savedOrder = new AtomicReference<>();
        when(orders.findFirstByPrescriptionId("RX-1")).thenReturn(Optional.empty());
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedOrder.set(order);
            return order;
        });
        when(orders.findById(anyString())).thenAnswer(invocation -> Optional.ofNullable(savedOrder.get()));
        when(orders.findByIdForUpdate(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(savedOrder.get()));
        OrderItemJpaRepository orderItems = mock(OrderItemJpaRepository.class);
        when(orderItems.findByOrderId(anyString())).thenReturn(List.of());
        OrderService service = service(orders, orderItems, inventory);
        Prescription prescription = prescription();

        Order order = service.createForPrescription(prescription, List.of(item("MED-1", 6)));

        assertEquals("RX-1", order.getPrescriptionId());
        assertEquals(OrderStatus.PROCESSING, order.getDispensingStatus());
        verify(inventory, times(2)).decrementStockAtomically(anyString(), anyInt(), any());
        verify(inventory, never()).save(any(Inventory.class));
        ArgumentCaptor<OrderItem> savedItems = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItems, times(2)).save(savedItems.capture());
        assertEquals(List.of(3, 3), savedItems.getAllValues().stream().map(OrderItem::getQuantity).toList());
        assertEquals(List.of("INV-EARLY", "INV-LATE"),
                savedItems.getAllValues().stream().map(OrderItem::getInventoryId).toList());
    }

    @Test
    void reportsAllInsufficientMedicationsWithoutCreatingOrder() {
        InventoryJpaRepository inventory = mock(InventoryJpaRepository.class);
        when(inventory.findByMedicationIdAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                anyString(), any())).thenReturn(List.of());
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        when(orders.findFirstByPrescriptionId("RX-1")).thenReturn(Optional.empty());
        OrderService service = service(orders, mock(OrderItemJpaRepository.class), inventory);

        InsufficientStockException error = assertThrows(InsufficientStockException.class,
                () -> service.createForPrescription(prescription(),
                        List.of(item("MED-1", 4), item("MED-2", 2))));

        assertEquals("Insufficient stock for medications: MED-1, MED-2", error.getMessage());
        assertEquals(List.of("MED-1", "MED-2"), error.getMedicineNames());
        verify(orders, never()).save(any(Order.class));
    }

    @Test
    void returnsExistingOrderInsteadOfCreatingDuplicate() {
        Order existing = new Order.Builder()
                .setOrderId("ORD-1")
                .setPatientId("PAT-1")
                .setPrescriptionId("RX-1")
                .setDispensingStatus(OrderStatus.PROCESSING)
                .build();
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        when(orders.findFirstByPrescriptionId("RX-1")).thenReturn(Optional.of(existing));
        when(orders.findById("ORD-1")).thenReturn(Optional.of(existing));
        when(orders.findByIdForUpdate("ORD-1")).thenReturn(Optional.of(existing));
        OrderItemJpaRepository items = mock(OrderItemJpaRepository.class);
        when(items.findByOrderId("ORD-1")).thenReturn(List.of());
        InventoryJpaRepository inventory = mock(InventoryJpaRepository.class);
        OrderService service = service(orders, items, inventory);

        Order result = service.createForPrescription(prescription(), List.of(item("MED-1", 1)));

        assertNotSame(existing, result);
        assertEquals(existing.getOrderId(), result.getOrderId());
        verify(orders, never()).save(any(Order.class));
        verify(inventory, never()).findByMedicationIdAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                anyString(), any());
    }

    @Test
    void cancellingPaidOrderRestocksAndRefunds() {
        Inventory batch = batch("INV-1", 2, LocalDate.now().plusDays(10));
        Order existing = new Order.Builder().setOrderId("ORD-1").setPatientId("PAT-1")
                .setDispensingStatus(OrderStatus.PROCESSING).setTotalAmount(new BigDecimal("5.00"))
                .setPaymentStatus(OrderPaymentStatus.PAID).setPaymentId("PAY-1").build();
        OrderItem item = new OrderItem.Builder().setOrderItemId("ITEM-1").setOrderId("ORD-1")
                .setMedicationId("MED-1").setInventoryId("INV-1").setQuantity(3)
                .setPrice(new BigDecimal("2.50")).build();
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        when(orders.findById("ORD-1")).thenReturn(Optional.of(existing));
        when(orders.findByIdForUpdate("ORD-1")).thenReturn(Optional.of(existing));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrderItemJpaRepository items = mock(OrderItemJpaRepository.class);
        when(items.findByOrderId("ORD-1")).thenReturn(List.of(item));
        InventoryJpaRepository inventory = mock(InventoryJpaRepository.class);
        when(inventory.findByIdForUpdate("INV-1")).thenReturn(Optional.of(batch));
        PaymentService payments = mock(PaymentService.class);
        OrderService service = new OrderService(orders, items, inventory,
                mock(MedicationJpaRepository.class), mock(AuditLogService.class), payments);

        service.updateStatus("ORD-1", OrderStatus.CANCELLED, "staff");

        verify(inventory).save(argThat(saved -> saved.getStockLevel() == 5));
        verify(payments).refundForCancelledOrder("ORD-1", "staff");
    }

    @Test
    void blocksDispensingUnpaidOrder() {
        Order existing = new Order.Builder().setOrderId("ORD-1").setPatientId("PAT-1")
                .setDispensingStatus(OrderStatus.PROCESSING).build();
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        when(orders.findByIdForUpdate("ORD-1")).thenReturn(Optional.of(existing));
        OrderService service = new OrderService(orders, mock(OrderItemJpaRepository.class),
                mock(InventoryJpaRepository.class), mock(MedicationJpaRepository.class),
                mock(AuditLogService.class));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.updateStatus("ORD-1", OrderStatus.DISPENSED, "staff"));

        assertEquals("Order must be paid before dispensing", error.getMessage());
    }

    private OrderService service(OrderJpaRepository orders, OrderItemJpaRepository items,
                                 InventoryJpaRepository inventory) {
        when(inventory.decrementStockAtomically(anyString(), anyInt(), any()))
                .thenReturn(1);
        return new OrderService(orders, items, inventory, mock(MedicationJpaRepository.class),
                new AuditLogService(mock(AuditLogJpaRepository.class)));
    }

    private Prescription prescription() {
        return new Prescription.Builder()
                .setPrescriptionId("RX-1")
                .setPatientId("PAT-1")
                .build();
    }

    private PrescriptionItem item(String medicationId, int quantity) {
        return new PrescriptionItem.Builder()
                .setPrescriptionItemId("RXI-" + medicationId)
                .setPrescriptionId("RX-1")
                .setMedicationId(medicationId)
                .setQuantity(quantity)
                .build();
    }

    private Inventory batch(String inventoryId, int stock, LocalDate expiry) {
        return new Inventory.Builder()
                .setInventoryId(inventoryId)
                .setMedicationId("MED-1")
                .setStockLevel(stock)
                .setExpiryDate(expiry)
                .setBatchNumber("B-" + inventoryId)
                .setUnitPrice(new BigDecimal("2.50"))
                .build();
    }
}
