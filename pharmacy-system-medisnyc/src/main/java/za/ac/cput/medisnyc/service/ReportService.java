package za.ac.cput.medisnyc.service;

/* ReportService.java
   Module 6: Reports & Administration - Reports and Dashboard Statistics.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.repository.jpa.AppointmentJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportService {

    private final OrderJpaRepository orderRepository;
    private final PrescriptionJpaRepository prescriptionRepository;
    private final PatientJpaRepository patientRepository;
    private final AppointmentJpaRepository appointmentRepository;
    private final InventoryJpaRepository inventoryRepository;

    @Autowired
    public ReportService(OrderJpaRepository orderRepository,
                         PrescriptionJpaRepository prescriptionRepository,
                         PatientJpaRepository patientRepository,
                         AppointmentJpaRepository appointmentRepository,
                         InventoryJpaRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Module 6: System Statistics / Dashboard Statistics API
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalAppointments", appointmentRepository.count());
        stats.put("totalPrescriptions", prescriptionRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("lowStockItems", inventoryRepository.findAll().stream().filter(i -> i.isLowStock()).count());
        stats.put("prescriptionsAwaitingCollection",
                prescriptionRepository.findByProcessingStatus(PrescriptionProcessingStatus.READY_FOR_COLLECTION).size());
        return stats;
    }

    // Module 6: Reports - revenue report from completed/dispensed orders
    public Map<String, Object> getRevenueReport() {
        BigDecimal total = orderRepository.findAll().stream()
                .filter(Order::isCompleted)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedOrders = orderRepository.findByDispensingStatus(OrderStatus.COMPLETED).size()
                + orderRepository.findByDispensingStatus(OrderStatus.DISPENSED).size();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalRevenue", total);
        report.put("completedOrders", completedOrders);
        return report;
    }
}