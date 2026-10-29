package za.ac.cput.medisnyc.service;

import za.ac.cput.medisnyc.domain.OrderPaymentStatus;
import za.ac.cput.medisnyc.domain.PaymentStatus;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.repository.jpa.AppointmentJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PaymentJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final OrderJpaRepository orderRepository;
    private final PrescriptionJpaRepository prescriptionRepository;
    private final PatientJpaRepository patientRepository;
    private final AppointmentJpaRepository appointmentRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final PaymentJpaRepository paymentRepository;

    public ReportService(OrderJpaRepository orderRepository,
                         PrescriptionJpaRepository prescriptionRepository,
                         PatientJpaRepository patientRepository,
                         AppointmentJpaRepository appointmentRepository,
                         InventoryJpaRepository inventoryRepository,
                         PaymentJpaRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalAppointments", appointmentRepository.count());
        stats.put("totalPrescriptions", prescriptionRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("lowStockItems", inventoryRepository.findAll().stream().filter(i -> i.isLowStock()).count());
        stats.put("prescriptionsAwaitingCollection",
                prescriptionRepository.findByProcessingStatus(PrescriptionProcessingStatus.READY_FOR_COLLECTION).size());
        LocalDate today = LocalDate.now();
        stats.put("paymentsToday", paymentRepository.countByStatusAndPaidAtRange(PaymentStatus.PAID,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        stats.put("outstandingAmount", orderRepository.sumOutstandingAmount(OrderPaymentStatus.UNPAID));
        return stats;
    }

    public Map<String, Object> getRevenueReport(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();
        Object[] totals = paymentRepository.revenueTotals(start, end);
        BigDecimal grossPaid = decimal(totals == null ? null : totals[0]);
        BigDecimal refunds = decimal(totals == null ? null : totals[1]);
        long paidOrders = number(totals == null ? null : totals[2]);
        long failedPayments = paymentRepository.countByStatusAndCreatedAtRange(
                PaymentStatus.FAILED, start, end);
        BigDecimal totalRevenue = grossPaid.subtract(refunds);
        BigDecimal averageOrderValue = paidOrders == 0 ? BigDecimal.ZERO.setScale(2)
                : totalRevenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);

        LocalDate dailyStart = from;
        LocalDate dailyEnd = to;
        if (dailyStart == null || dailyEnd == null) {
            LocalDate earliest = paymentRepository.findFirstByPaidAtNotNullOrderByPaidAtAsc()
                    .map(p -> p.getPaidAt().toLocalDate()).orElse(LocalDate.now());
            if (dailyStart == null) dailyStart = earliest;
            if (dailyEnd == null) dailyEnd = LocalDate.now();
            if (dailyEnd.isBefore(dailyStart)) dailyEnd = dailyStart;
        }
        LocalDateTime dailyStartTime = dailyStart.atStartOfDay();
        LocalDateTime dailyEndTime = dailyEnd.plusDays(1).atStartOfDay();
        Map<LocalDate, BigDecimal> amountsByDate = new LinkedHashMap<>();
        for (Object[] row : paymentRepository.revenueByDay(dailyStartTime, dailyEndTime)) {
            amountsByDate.put(localDate(row[0]), decimal(row[1]));
        }
        List<Map<String, Object>> byDay = new ArrayList<>();
        for (LocalDate day = dailyStart; !day.isAfter(dailyEnd); day = day.plusDays(1)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", day);
            entry.put("amount", amountsByDate.getOrDefault(day, BigDecimal.ZERO));
            byDay.add(entry);
        }

        List<Map<String, Object>> byCardBrand = new ArrayList<>();
        for (Object[] row : paymentRepository.revenueByCardBrand(dailyStartTime, dailyEndTime)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("brand", row[0]);
            entry.put("amount", decimal(row[1]));
            entry.put("count", number(row[2]));
            byCardBrand.add(entry);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("grossPaid", grossPaid);
        report.put("refunds", refunds);
        report.put("paidOrders", paidOrders);
        report.put("unpaidOrders", orderRepository.countUnpaidOrders(OrderPaymentStatus.UNPAID));
        report.put("outstandingAmount", orderRepository.sumOutstandingAmount(OrderPaymentStatus.UNPAID));
        report.put("failedPayments", failedPayments);
        report.put("averageOrderValue", averageOrderValue);
        report.put("byDay", byDay);
        report.put("byCardBrand", byCardBrand);
        return report;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal amount) return amount;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return BigDecimal.ZERO;
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private LocalDate localDate(Object value) {
        if (value instanceof LocalDate date) return date;
        if (value instanceof Date date) return date.toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
