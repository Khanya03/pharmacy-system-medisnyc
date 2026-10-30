package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.domain.Payment;
import za.ac.cput.medisnyc.domain.PaymentStatus;
import za.ac.cput.medisnyc.repository.jpa.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportServiceTest {
    @Test
    void revenueIsPaidMinusRefundsAndUsesRequestedDateRange() {
        OrderJpaRepository orders = mock(OrderJpaRepository.class);
        PaymentJpaRepository payments = mock(PaymentJpaRepository.class);
        when(payments.revenueTotals(any(), any())).thenReturn(new Object[]{
                new BigDecimal("100.00"), new BigDecimal("25.00"), 2L});
        when(payments.countByStatusAndCreatedAtRange(eq(PaymentStatus.FAILED), any(), any())).thenReturn(3L);
        when(payments.revenueByDay(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{LocalDate.of(2026, 10, 1), new BigDecimal("75.00")}));
        when(payments.revenueByCardBrand(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{"Visa", new BigDecimal("75.00"), 2L}));
        when(orders.countUnpaidOrders(any())).thenReturn(4L);
        when(orders.sumOutstandingAmount(any())).thenReturn(new BigDecimal("80.00"));
        ReportService service = new ReportService(orders, mock(PrescriptionJpaRepository.class),
                mock(PatientJpaRepository.class), mock(AppointmentJpaRepository.class),
                mock(InventoryJpaRepository.class), payments);

        Map<String, Object> report = service.getRevenueReport(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));

        assertEquals(new BigDecimal("75.00"), report.get("totalRevenue"));
        assertEquals(new BigDecimal("100.00"), report.get("grossPaid"));
        assertEquals(new BigDecimal("25.00"), report.get("refunds"));
        assertEquals(2L, report.get("paidOrders"));
        assertEquals(4L, report.get("unpaidOrders"));
        assertEquals(3L, report.get("failedPayments"));
        assertEquals(2, ((List<?>) report.get("byDay")).size());
        verify(payments).revenueTotals(
                LocalDate.of(2026, 10, 1).atStartOfDay(),
                LocalDate.of(2026, 10, 3).atStartOfDay());
    }

    @Test
    void csvExportContainsOnlyPaymentMetadataAndLastFour() {
        Payment payment = new za.ac.cput.medisnyc.domain.Payment.Builder()
                .setPaymentId("PAY-12345678").setReceiptNumber("RCP-20261001-0001")
                .setOrderId("ORD-1").setPatientId("PAT-1").setAmount(new BigDecimal("10.00"))
                .setIdempotencyKey("key").setStatus(PaymentStatus.PAID).setCardBrand("Visa")
                .setCardLast4("4242").setPaidAt(java.time.LocalDateTime.now()).build();
        PaymentJpaRepository payments = mock(PaymentJpaRepository.class);
        when(payments.findForExport(any(), any())).thenReturn(List.of(payment));
        PaymentService service = new PaymentService(payments, mock(OrderJpaRepository.class),
                mock(OrderItemJpaRepository.class), mock(PatientJpaRepository.class),
                mock(MedicationJpaRepository.class), mock(PaymentGateway.class),
                mock(NotificationService.class), mock(AuditLogService.class));

        Map<String, Object> row = service.export(null, null).get(0);
        String csv = row.toString();
        assertEquals("4242", row.get("last4"));
        org.junit.jupiter.api.Assertions.assertFalse(csv.contains("4242424242424242"));
        org.junit.jupiter.api.Assertions.assertFalse(csv.toLowerCase().contains("cvv"));
    }

    @Test
    void rejectsRevenueReportWhenFromIsAfterTo() {
        ReportService service = new ReportService(mock(OrderJpaRepository.class),
                mock(PrescriptionJpaRepository.class), mock(PatientJpaRepository.class),
                mock(AppointmentJpaRepository.class), mock(InventoryJpaRepository.class),
                mock(PaymentJpaRepository.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.getRevenueReport(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 1)));

        assertTrue(ex.getMessage().contains("from"));
    }
}
