package za.ac.cput.medisnyc.controller;


/* InventoryController.java
   Module 4: Pharmacy Inventory Module - Update Stock/Low Stock Alerts.
   Author: Thakane Jeanet Moloi
*/

import za.ac.cput.medisnyc.domain.Inventory;
import za.ac.cput.medisnyc.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<Inventory> getById(@PathVariable String inventoryId) {
        return ResponseEntity.ok(inventoryService.getById(inventoryId));
    }

    @GetMapping("/medication/{medicationId}")
    public ResponseEntity<List<Inventory>> getByMedication(@PathVariable String medicationId) {
        return ResponseEntity.ok(inventoryService.getByMedication(medicationId));
    }

    @PostMapping
    public ResponseEntity<Inventory> receiveStock(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.receiveStock(inventory));
    }

    @PutMapping("/{inventoryId}/stock")
    public ResponseEntity<Inventory> updateStock(@PathVariable String inventoryId,
                                                 @RequestParam int stockLevel) {
        return ResponseEntity.ok(inventoryService.updateStock(inventoryId, stockLevel));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> lowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Inventory>> expiringSoon(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(inventoryService.getExpiringSoon(days));
    }
}