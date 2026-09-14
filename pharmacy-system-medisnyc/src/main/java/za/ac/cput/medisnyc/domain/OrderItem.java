package za.ac.cput.medisnyc.domain;
/* OrderItem.java
   OrderItem model class
   Author: Phemelo Molefi (230255299)
   Date: 19 July 2026
*/
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    private String orderItemId;
    private String orderId;
    private String medicationId;
    private String inventoryId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    protected OrderItem() {
    }

    private OrderItem(Builder builder) {
        this.orderItemId = builder.orderItemId;
        this.orderId = builder.orderId;
        this.medicationId = builder.medicationId;
        this.inventoryId = builder.inventoryId;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public String getOrderItemId() { return orderItemId; }
    public String getOrderId() { return orderId; }
    public String getMedicationId() { return medicationId; }
    public String getInventoryId() { return inventoryId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getSubtotal() { return subtotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId='" + orderItemId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", medicationId='" + medicationId + '\'' +
                ", inventoryId='" + inventoryId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
    public static class Builder {
        private String orderItemId;
        private String orderId;
        private String medicationId;
        private String inventoryId;
        private int quantity;
        private BigDecimal price;

        public Builder setOrderItemId(String orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setMedicationId(String medicationId) {
            this.medicationId = medicationId;
            return this;
        }

        public Builder setInventoryId(String inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

        public Builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public OrderItem build() {
            if (orderItemId == null || orderItemId.isBlank()) {
                throw new IllegalArgumentException("Order item ID is required");
            }
            if (orderId == null || orderId.isBlank()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (medicationId == null || medicationId.isBlank()) {
                throw new IllegalArgumentException("Medication ID is required");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valid price is required");
            }
            return new OrderItem(this);
        }
    }
}