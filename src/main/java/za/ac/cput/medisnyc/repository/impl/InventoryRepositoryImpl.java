package za.ac.cput.medisnyc.repository.impl;

/* InventoryRepositoryImpl.java
   Inventory Repository implementation
   Author: [Thakane Jeanet Moloi] ([230186904])
   Date: 16 March 2026
*/

import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryRepositoryImpl implements InventoryRepository {

    private final Map<String, Inventory> inventoryMap = new HashMap<>();
    private static InventoryRepositoryImpl repository = null;

    private InventoryRepositoryImpl() {}

    public static InventoryRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new InventoryRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Inventory create(Inventory inventory) {
        if (inventory == null) return null;
        inventoryMap.put(inventory.getInventoryId(), inventory);
        return inventory;
    }

    @Override
    public Inventory read(String inventoryId) {
        return inventoryMap.get(inventoryId);
    }

    @Override
    public Inventory update(Inventory inventory) {
        if (inventory == null || !inventoryMap.containsKey(inventory.getInventoryId())) {
            return null;
        }
        inventoryMap.put(inventory.getInventoryId(), inventory);
        return inventory;
    }

    @Override
    public boolean delete(String inventoryId) {
        return inventoryMap.remove(inventoryId) != null;
    }

    @Override
    public List<Inventory> getAll() {
        return new ArrayList<>(inventoryMap.values());
    }

    @Override
    public List<Inventory> findByMedicationId(String medicationId) {
        return inventoryMap.values().stream()
                .filter(i -> i.getMedicationId().equalsIgnoreCase(medicationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findLowStockItems() {
        return inventoryMap.values().stream()
                .filter(Inventory::isLowStock)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findExpiredItems() {
        return inventoryMap.values().stream()
                .filter(Inventory::isExpired)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findExpiringItems(int days) {
        return inventoryMap.values().stream()
                .filter(i -> i.isExpiringSoon(days))
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findByBatchNumber(String batchNumber) {
        return inventoryMap.values().stream()
                .filter(i -> i.getBatchNumber().equalsIgnoreCase(batchNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStockLevel(String inventoryId, int newStockLevel) {
        Inventory current = read(inventoryId);
        if (current != null && newStockLevel >= 0) {
            Inventory updated = new Inventory.Builder()
                    .setInventoryId(current.getInventoryId())
                    .setMedicationId(current.getMedicationId())
                    .setStockLevel(newStockLevel)
                    .setExpiryDate(current.getExpiryDate())
                    .setBatchNumber(current.getBatchNumber())
                    .setSupplier(current.getSupplier())
                    .setUnitPrice(current.getUnitPrice())
                    .setReceivedDate(current.getReceivedDate())
                    .setReorderLevel(current.getReorderLevel())
                    .build();
            update(updated);
            return true;
        }
        return false;
    }
}