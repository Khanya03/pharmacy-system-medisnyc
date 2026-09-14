package za.ac.cput.medisnyc.controller;

/* PrescriptionProcessingController.java
   Module 5: Prescription Processing Module - Update Status API / queue / collection.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.service.PrescriptionProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescription-processing")
public class PrescriptionProcessingController {

    private final PrescriptionProcessingService processingService;

    @Autowired
    public PrescriptionProcessingController(PrescriptionProcessingService processingService) {
        this.processingService = processingService;
    }

    @GetMapping("/queue")
    public ResponseEntity<List<Prescription>> getQueue() {
        return ResponseEntity.ok(processingService.getQueue());
    }

    // Module 5: Prescription Tracking page - a patient looking up their own prescriptions.
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(processingService.getByPatient(patientId));
    }

    @GetMapping("/ready-for-collection")
    public ResponseEntity<List<Prescription>> getReadyForCollection() {
        return ResponseEntity.ok(processingService.getReadyForCollection());
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<Prescription> track(@PathVariable String prescriptionId) {
        return ResponseEntity.ok(processingService.getById(prescriptionId));
    }

    // Advances Pending -> Received -> Preparing -> Ready for Collection -> Collected,
    // one step per call, and fires the patient notification automatically.
    @PutMapping("/{prescriptionId}/advance")
    public ResponseEntity<Prescription> advanceStatus(@PathVariable String prescriptionId) {
        return ResponseEntity.ok(processingService.advanceStatus(prescriptionId));
    }
}