package za.ac.cput.medisnyc.repository;

/* InventoryRepository.java
   Inventory Repository interface
   Author: [Thakane Jeanet Moloi] ([230186904])
   Date: 16 March 2026
*/



import za.ac.cput.medisnyc.domain.Inventory;

import java.util.List;

public interface InventoryRepository extends IRepository<Inventory, String> {
    List<Inventory> findByMedicationId(String medicationId);
    List<Inventory> findLowStockItems();
    List<Inventory> findExpiredItems();
    List<Inventory> findExpiringItems(int days);
    List<Inventory> findByBatchNumber(String batchNumber);
    boolean updateStockLevel(String inventoryId, int newStockLevel);
}