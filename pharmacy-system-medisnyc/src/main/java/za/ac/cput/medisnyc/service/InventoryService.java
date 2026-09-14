package za.ac.cput.medisnyc.service;

/* InventoryService.java
   Module 4: Pharmacy Inventory Module (stock levels, low stock alerts).
   Author: Thakane Jeanet Moloi
*/

import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.repository.jpa.InventoryJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryJpaRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryJpaRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    public Inventory getById(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory record not found: " + inventoryId));
    }

    public List<Inventory> getByMedication(String medicationId) {
        return inventoryRepository.findByMedicationId(medicationId);
    }

    @Transactional
    public Inventory receiveStock(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateStock(String inventoryId, int newStockLevel) {
        Inventory existing = getById(inventoryId);
        Inventory updated = new Inventory.Builder()
                .setInventoryId(existing.getInventoryId())
                .setMedicationId(existing.getMedicationId())
                .setStockLevel(newStockLevel)
                .setExpiryDate(existing.getExpiryDate())
                .setBatchNumber(existing.getBatchNumber())
                .setSupplier(existing.getSupplier())
                .setUnitPrice(existing.getUnitPrice())
                .setReceivedDate(existing.getReceivedDate())
                .setReorderLevel(existing.getReorderLevel())
                .build();
        return inventoryRepository.save(updated);
    }

    // Module 4: Low Stock Alerts
    public List<Inventory> getLowStockAlerts() {
        return inventoryRepository.findAll().stream()
                .filter(Inventory::isLowStock)
                .toList();
    }

    public List<Inventory> getExpiringSoon(int days) {
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.isExpiringSoon(days))
                .toList();
    }
}