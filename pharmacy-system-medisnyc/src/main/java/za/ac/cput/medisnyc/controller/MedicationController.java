package za.ac.cput.medisnyc.controller;

/* MedicationController.java
   Module 4: Pharmacy Inventory Module - Medicine List/Add Medicine.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    @Autowired
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public ResponseEntity<List<Medication>> getAll() {
        return ResponseEntity.ok(medicationService.getAll());
    }

    @GetMapping("/{medicationId}")
    public ResponseEntity<Medication> getById(@PathVariable String medicationId) {
        return ResponseEntity.ok(medicationService.getById(medicationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Medication>> search(@RequestParam String name) {
        return ResponseEntity.ok(medicationService.search(name));
    }

    @PostMapping
    public ResponseEntity<Medication> addMedicine(@RequestBody Medication medication) {
        return ResponseEntity.ok(medicationService.addMedicine(medication));
    }

    @PutMapping("/{medicationId}")
    public ResponseEntity<Medication> updateMedicine(@PathVariable String medicationId,
                                                     @RequestBody Medication medication) {
        return ResponseEntity.ok(medicationService.updateMedicine(medicationId, medication));
    }

    @DeleteMapping("/{medicationId}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable String medicationId) {
        medicationService.deleteMedicine(medicationId);
        return ResponseEntity.noContent().build();
    }
}