package za.ac.cput.medisnyc.factory;

/* InventoryFactory.java
   Inventory factory class
   Author: [Thakane Jeanet Moloi] ([230186904])
   Date: 16 March 2026
*/



import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.util.Helper;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryFactory {

    public static Inventory createInventory(String inventoryId, String medicationId,
                                            int stockLevel, LocalDate expiryDate,
                                            String batchNumber, String supplier,
                                            BigDecimal unitPrice, LocalDate receivedDate,
                                            int reorderLevel) {

        if (Helper.isNullOrEmpty(inventoryId)) {
            throw new IllegalArgumentException("Inventory ID cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(medicationId)) {
            throw new IllegalArgumentException("Medication ID cannot be null or empty");
        }
        if (stockLevel < 0) {
            throw new IllegalArgumentException("Stock level cannot be negative");
        }
        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Valid future expiry date is required");
        }
        if (Helper.isNullOrEmpty(batchNumber)) {
            throw new IllegalArgumentException("Batch number cannot be null or empty");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid positive unit price is required");
        }

        return new Inventory.Builder()
                .setInventoryId(inventoryId.trim().toUpperCase())
                .setMedicationId(medicationId.trim().toUpperCase())
                .setStockLevel(stockLevel)
                .setExpiryDate(expiryDate)
                .setBatchNumber(batchNumber.trim().toUpperCase())
                .setSupplier(supplier != null ? Helper.capitalizeFirstLetter(supplier.trim()) : null)
                .setUnitPrice(unitPrice)
                .setReceivedDate(receivedDate)
                .setReorderLevel(reorderLevel > 0 ? reorderLevel : 10)
                .build();
    }

    public static Inventory createInventory(String inventoryId, String medicationId,
                                            int stockLevel, LocalDate expiryDate,
                                            String batchNumber, BigDecimal unitPrice) {
        return createInventory(inventoryId, medicationId, stockLevel, expiryDate,
                batchNumber, null, unitPrice, LocalDate.now(), 10);
    }
}