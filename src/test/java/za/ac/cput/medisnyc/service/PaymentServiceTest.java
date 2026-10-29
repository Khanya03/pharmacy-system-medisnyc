package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import za.ac.cput.medisnyc.domain.*;
import za.ac.cput.medisnyc.dto.CardPaymentRequest;
import za.ac.cput.medisnyc.exception.PaymentException;
import za.ac.cput.medisnyc.repository.jpa.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    @Test
    void chargesServerAmountAndClearsCardDto() {
        Fixture fixture = new Fixture();
        CardPaymentRequest request = request("key-1");

        var response = fixture.service.payByCard(request, "PAT-1", true, "patient");

        assertEquals(new BigDecimal("25.00"), response.amount());
        assertEquals("ZAR", response.currency());
        assertEquals("PAID", response.status());
        assertNull(request.getCardNumber());
        assertNull(request.getCvv());
        assertNull(request.getCardholderName());
        verify(fixture.orderRepository).findByIdForUpdate("ORD-1");
    }

    @Test
    void idempotencyReturnsTheOriginalPaymentWithoutChargingAgain() {
        Fixture fixture = new Fixture();
        java.util.concurrent.atomic.AtomicReference<Payment> saved = new java.util.concurrent.atomic.AtomicReference<>();
        when(fixture.paymentRepository.findByOrderIdAndIdempotencyKey("ORD-1", "same-key"))
                .thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(fixture.paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            saved.set(payment);
            return payment;
        });
        var first = fixture.service.payByCard(request("same-key"), "PAT-1", true, "patient");
        var second = fixture.service.payByCard(request("same-key"), "PAT-1", true, "patient");

        assertEquals(first.paymentId(), second.paymentId());
        verify(fixture.gateway, times(1)).charge(any());
    }

    @Test
    void blocksDoublePaymentAndOtherPatients() {
        Fixture alreadyPaid = new Fixture(order(OrderPaymentStatus.PAID));
        PaymentException paid = assertThrows(PaymentException.class, () -> alreadyPaid.service
                .payByCard(request("new-key"), "PAT-1", true, "patient"));
        assertEquals(409, paid.getStatus().value());
        verify(alreadyPaid.gateway, never()).charge(any());

        Fixture otherPatient = new Fixture();
        assertThrows(AccessDeniedException.class, () -> otherPatient.service
                .payByCard(request("key"), "PAT-2", true, "patient"));
        verify(otherPatient.gateway, never()).charge(any());
    }

    @Test
    void fifthFailedAttemptIsSavedAndNextAttemptIsRateLimited() {
        Fixture fixture = new Fixture();
        AtomicInteger failures = new AtomicInteger();
        when(fixture.paymentRepository.countByOrderIdAndStatusAndCreatedAtAfter(
                eq("ORD-1"), eq(PaymentStatus.FAILED), any())).thenAnswer(invocation -> (long) failures.get());
        when(fixture.gateway.charge(any())).thenReturn(new PaymentGateway.GatewayResult(false, "Visa", "0002",
                "SIM-FAIL", "CARD_DECLINED", "Card was declined"));
        when(fixture.paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            failures.incrementAndGet();
            return invocation.getArgument(0);
        });
        for (int i = 0; i < 5; i++) {
            PaymentException error = assertThrows(PaymentException.class, () -> fixture.service
                    .payByCard(request("failed-" + failures.get()), "PAT-1", true, "patient"));
            assertEquals(402, error.getStatus().value());
        }
        PaymentException limited = assertThrows(PaymentException.class, () -> fixture.service
                .payByCard(request("sixth"), "PAT-1", true, "patient"));
        assertEquals(429, limited.getStatus().value());
        verify(fixture.paymentRepository, times(5)).save(any(Payment.class));
    }

    private CardPaymentRequest request(String key) {
        CardPaymentRequest request = new CardPaymentRequest();
        request.setOrderId("ORD-1");
        request.setCardholderName("Test Patient");
        request.setCardNumber("4242424242424242");
        request.setExpiryMonth(12);
        request.setExpiryYear(2030);
        request.setCvv("123");
        request.setIdempotencyKey(key);
        return request;
    }

    private Payment payment(String id, String key) {
        return new Payment.Builder().setPaymentId(id).setOrderId("ORD-1").setPatientId("PAT-1")
                .setAmount(new BigDecimal("25.00")).setCurrency("ZAR").setStatus(PaymentStatus.PAID)
                .setIdempotencyKey(key).setReceiptNumber("RCP-20261029-0001")
                .setCardBrand("Visa").setCardLast4("4242").setPaidAt(java.time.LocalDateTime.now()).build();
    }

    private static Order order(OrderPaymentStatus status) {
        return new Order.Builder().setOrderId("ORD-1").setPatientId("PAT-1")
                .setTotalAmount(new BigDecimal("25.00")).setPaymentStatus(status).build();
    }

    private class Fixture {
        final PaymentJpaRepository paymentRepository = mock(PaymentJpaRepository.class);
        final OrderJpaRepository orderRepository = mock(OrderJpaRepository.class);
        final OrderItemJpaRepository orderItemRepository = mock(OrderItemJpaRepository.class);
        final PatientJpaRepository patientRepository = mock(PatientJpaRepository.class);
        final MedicationJpaRepository medicationRepository = mock(MedicationJpaRepository.class);
        final PaymentGateway gateway = mock(PaymentGateway.class);
        final PaymentService service;

        Fixture() { this(order(OrderPaymentStatus.UNPAID)); }

        Fixture(Order order) {
            when(orderRepository.findByIdForUpdate("ORD-1")).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(orderItemRepository.findByOrderId("ORD-1")).thenReturn(List.of(
                    new OrderItem.Builder().setOrderItemId("ITEM-1").setOrderId("ORD-1")
                            .setMedicationId("MED-1").setInventoryId("INV-1").setQuantity(1)
                            .setPrice(new BigDecimal("25.00")).build()));
            when(paymentRepository.findByOrderIdAndIdempotencyKey(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(paymentRepository.countByOrderIdAndStatusAndCreatedAtAfter(anyString(), any(), any()))
                    .thenReturn(0L);
            when(paymentRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(gateway.charge(any())).thenReturn(new PaymentGateway.GatewayResult(true, "Visa", "4242",
                    "SIM-TEST", null, null));
            service = new PaymentService(paymentRepository, orderRepository, orderItemRepository,
                    patientRepository, medicationRepository, gateway, mock(NotificationService.class),
                    mock(AuditLogService.class));
        }
    }
}
