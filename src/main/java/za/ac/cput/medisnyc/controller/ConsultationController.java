package za.ac.cput.medisnyc.controller;

/* ConsultationController.java
   Module 3: Doctor Consultation & Prescription Module.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.MedicalRecord;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionItem;
import za.ac.cput.medisnyc.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConsultationController {

    private final ConsultationService consultationService;

    @Autowired
    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping("/api/medical-records")
    public ResponseEntity<MedicalRecord> createMedicalRecord(@RequestBody MedicalRecord record) {
        return ResponseEntity.ok(consultationService.createMedicalRecord(record));
    }

    @GetMapping("/api/medical-records/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(consultationService.getRecordsByPatient(patientId));
    }

    @GetMapping("/api/medical-records/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecord>> getByDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(consultationService.getRecordsByDoctor(doctorId));
    }

    public static class PrescriptionRequest {
        public Prescription prescription;
        public List<PrescriptionItem> items;
    }

    @PostMapping("/api/consultations/prescriptions")
    public ResponseEntity<Prescription> createPrescription(@RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(consultationService.createPrescription(request.prescription, request.items));
    }

    @GetMapping("/api/consultations/prescriptions/{prescriptionId}/items")
    public ResponseEntity<List<PrescriptionItem>> getItems(@PathVariable String prescriptionId) {
        return ResponseEntity.ok(consultationService.getItemsForPrescription(prescriptionId));
    }
}
