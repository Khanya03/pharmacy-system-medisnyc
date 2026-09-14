package za.ac.cput.medisnyc.factory;


/* OrderFactory.java
   Order factory class
   Author: Phemelo Molefi (230255299)
   Date: 19 March 2026
*/



import za.ac.cput.domain.Order;
import za.ac.cput.domain.OrderItem;
import za.ac.cput.domain.OrderStatus;
import za.ac.cput.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderFactory {

    public static Order createOrder(String orderId, String patientId,
                                    String prescriptionId, LocalDateTime orderDate,
                                    OrderStatus status, BigDecimal totalAmount,
                                    String pharmacistId, String notes,
                                    List<OrderItem> items) {

        if (Helper.isNullOrEmpty(orderId)) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(patientId)) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }

        return new Order.Builder()
                .setOrderId(orderId.trim().toUpperCase())
                .setPatientId(patientId.trim().toUpperCase())
                .setPrescriptionId(prescriptionId != null ? prescriptionId.trim().toUpperCase() : null)
                .setOrderDate(orderDate != null ? orderDate : LocalDateTime.now())
                .setDispensingStatus(status != null ? status : OrderStatus.PENDING)
                .setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .setPharmacistId(pharmacistId != null ? pharmacistId.trim() : null)
                .setNotes(notes != null ? notes.trim() : null)
                .setItems(items)
                .build();
    }

    public static Order createOrder(String orderId, String patientId,
                                    String prescriptionId, List<OrderItem> items) {
        return createOrder(orderId, patientId, prescriptionId, null,
                null, null, null, null, items);
    }
}