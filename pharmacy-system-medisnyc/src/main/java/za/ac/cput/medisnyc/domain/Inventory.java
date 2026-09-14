package za.ac.cput.medisnyc.domain;

/* Inventory.java
   Inventory model class
   Author: [Thakane Jeanet Moloi] ([230186904])
   Date: 16 March 2026
*/



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    private String inventoryId;
    private String medicationId;
    private int stockLevel;
    private LocalDate expiryDate;
    private String batchNumber;
    private String supplier;
    private BigDecimal unitPrice;
    private LocalDate receivedDate;
    private int reorderLevel;

    protected Inventory() {
    }

    private Inventory(Builder builder) {
        this.inventoryId = builder.inventoryId;
        this.medicationId = builder.medicationId;
        this.stockLevel = builder.stockLevel;
        this.expiryDate = builder.expiryDate;
        this.batchNumber = builder.batchNumber;
        this.supplier = builder.supplier;
        this.unitPrice = builder.unitPrice;
        this.receivedDate = builder.receivedDate;
        this.reorderLevel = builder.reorderLevel;
    }

    // Getters
    public String getInventoryId() { return inventoryId; }
    public String getMedicationId() { return medicationId; }
    public int getStockLevel() { return stockLevel; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getBatchNumber() { return batchNumber; }
    public String getSupplier() { return supplier; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public int getReorderLevel() { return reorderLevel; }

    public boolean isLowStock() {
        return stockLevel <= reorderLevel;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon(int days) {
        return LocalDate.now().plusDays(days).isAfter(expiryDate) && !isExpired();
    }

    public boolean canFulfillOrder(int quantity) {
        return stockLevel >= quantity && !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(inventoryId, inventory.inventoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventoryId);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId='" + inventoryId + '\'' +
                ", medicationId='" + medicationId + '\'' +
                ", stockLevel=" + stockLevel +
                ", expiryDate=" + expiryDate +
                ", batchNumber='" + batchNumber + '\'' +
                ", supplier='" + supplier + '\'' +
                ", unitPrice=" + unitPrice +
                ", receivedDate=" + receivedDate +
                ", reorderLevel=" + reorderLevel +
                '}';
    }

    public static class Builder {
        private String inventoryId;
        private String medicationId;
        private int stockLevel;
        private LocalDate expiryDate;
        private String batchNumber;
        private String supplier;
        private BigDecimal unitPrice;
        private LocalDate receivedDate;
        private int reorderLevel = 10;

        public Builder setInventoryId(String inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

        public Builder setMedicationId(String medicationId) {
            this.medicationId = medicationId;
            return this;
        }

        public Builder setStockLevel(int stockLevel) {
            this.stockLevel = stockLevel;
            return this;
        }

        public Builder setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder setBatchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }

        public Builder setSupplier(String supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder setReceivedDate(LocalDate receivedDate) {
            this.receivedDate = receivedDate;
            return this;
        }

        public Builder setReorderLevel(int reorderLevel) {
            this.reorderLevel = reorderLevel;
            return this;
        }

        public Inventory build() {
            if (inventoryId == null || inventoryId.isBlank()) {
                throw new IllegalArgumentException("Inventory ID is required");
            }
            if (medicationId == null || medicationId.isBlank()) {
                throw new IllegalArgumentException("Medication ID is required");
            }
            if (stockLevel < 0) {
                throw new IllegalArgumentException("Stock level cannot be negative");
            }
            if (expiryDate == null) {
                throw new IllegalArgumentException("Expiry date is required");
            }
            if (batchNumber == null || batchNumber.isBlank()) {
                throw new IllegalArgumentException("Batch number is required");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valid unit price is required");
            }
            if (receivedDate == null) {
                this.receivedDate = LocalDate.now();
            }
            return new Inventory(this);
        }
    }
}
