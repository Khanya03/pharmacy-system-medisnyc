package za.ac.cput.medisnyc.repository;


/* OrderRepository.java
   Order repository interface
   Author: Phemelo Molefi (230255299)
   Date: 20 March 2026
*/

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends IRepository<Order, String> {
    List<Order> findByPatientId(String patientId);
    List<Order> findByPrescriptionId(String prescriptionId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findOrdersBetweenDates(LocalDateTime start, LocalDateTime end);
    List<Order> findPendingOrders();

}