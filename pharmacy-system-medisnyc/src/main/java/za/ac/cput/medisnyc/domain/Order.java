package za.ac.cput.medisnyc.domain;

/* Order.java
   Order model class
   Author: Phemelo Molefi (230255299)
   Date: 19 July 2026
*/

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String orderId;
    private String patientId;
    private String prescriptionId;
    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus dispensingStatus;
    private BigDecimal totalAmount;
    private String pharmacistId;
    private String notes;

    // OrderItem links back via a plain orderId string (not a JPA object reference),
    // so items are loaded separately by OrderItemRepository/OrderService rather than
    // through a JPA @OneToMany relationship.
    @Transient
    private List<OrderItem> items;

    protected Order() {
    }
    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.patientId = builder.patientId;
        this.prescriptionId = builder.prescriptionId;
        this.orderDate = builder.orderDate;
        this.dispensingStatus = builder.dispensingStatus;
        this.totalAmount = builder.totalAmount;
        this.pharmacistId = builder.pharmacistId;
        this.notes = builder.notes;
        this.items = builder.items != null ?
                new ArrayList<>(builder.items) : new ArrayList<>();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getPatientId() { return patientId; }
    public String getPrescriptionId() { return prescriptionId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public OrderStatus getDispensingStatus() { return dispensingStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPharmacistId() { return pharmacistId; }
    public String getNotes() { return notes; }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean canBeCancelled() {
        return dispensingStatus == OrderStatus.PENDING ||
                dispensingStatus == OrderStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return dispensingStatus == OrderStatus.DISPENSED ||
                dispensingStatus == OrderStatus.COMPLETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", prescriptionId='" + prescriptionId + '\'' +
                ", orderDate=" + orderDate +
                ", dispensingStatus=" + dispensingStatus +
                ", totalAmount=" + totalAmount +
                ", pharmacistId='" + pharmacistId + '\'' +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                '}';
    }

    public static class Builder {
        private String orderId;
        private String patientId;
        private String prescriptionId;
        private LocalDateTime orderDate;
        private OrderStatus dispensingStatus;
        private BigDecimal totalAmount;
        private String pharmacistId;
        private String notes;
        private List<OrderItem> items = new ArrayList<>();

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder setDispensingStatus(OrderStatus dispensingStatus) {
            this.dispensingStatus = dispensingStatus;
            return this;
        }

        public Builder setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder setPharmacistId(String pharmacistId) {
            this.pharmacistId = pharmacistId;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder setItems(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder addItem(OrderItem item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            this.items.add(item);
            return this;
        }

        public Order build() {
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (patientId == null || patientId.isBlank()) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            if (orderDate == null) {
                this.orderDate = LocalDateTime.now();
            }
            if (dispensingStatus == null) {
                this.dispensingStatus = OrderStatus.PENDING;
            }
            if (totalAmount == null) {
                this.totalAmount = BigDecimal.ZERO;
            }
            return new Order(this);
        }
    }
}