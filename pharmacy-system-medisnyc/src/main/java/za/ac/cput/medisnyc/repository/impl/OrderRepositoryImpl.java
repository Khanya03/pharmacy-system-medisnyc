package za.ac.cput.medisnyc.repository.impl;


/* OrderRepositoryImpl.java
   Order Repository implementation
   Author: Phemelo Molefi (230255299)
   Date: 19 March 2026
*/

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderRepositoryImpl implements OrderRepository {

    private final Map<String, Order> orderMap = new HashMap<>();
    private static OrderRepositoryImpl repository = null;

    private OrderRepositoryImpl() {}

    public static OrderRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new OrderRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Order create(Order order) {
        if (order == null) return null;
        orderMap.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Order read(String orderId) {
        return orderMap.get(orderId);  // Returns null if not found
    }

    @Override
    public Order update(Order order) {
        if (order == null || !orderMap.containsKey(order.getOrderId())) {
            return null;
        }
        orderMap.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public boolean delete(String orderId) {
        return orderMap.remove(orderId) != null;
    }

    @Override
    public List<Order> getAll() {
        return new ArrayList<>(orderMap.values());
    }

    @Override
    public List<Order> findByPatientId(String patientId) {
        return orderMap.values().stream()
                .filter(o -> patientId != null && patientId.equalsIgnoreCase(o.getPatientId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByPrescriptionId(String prescriptionId) {
        if (prescriptionId == null) return new ArrayList<>();
        return orderMap.values().stream()
                .filter(o -> prescriptionId.equalsIgnoreCase(o.getPrescriptionId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderMap.values().stream()
                .filter(o -> o.getDispensingStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderMap.values().stream()
                .filter(o -> !o.getOrderDate().isBefore(start) && !o.getOrderDate().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findPendingOrders() {
        return findByStatus(OrderStatus.PENDING);
    }


}