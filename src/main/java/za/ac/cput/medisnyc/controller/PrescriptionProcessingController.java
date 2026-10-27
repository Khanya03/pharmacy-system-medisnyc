package za.ac.cput.medisnyc.controller;

/* PrescriptionProcessingController.java
   Module 5: Prescription Processing Module - Update Status API / queue / collection.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.dto.PrescriptionQueueItem;
import za.ac.cput.medisnyc.service.PrescriptionProcessingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<PrescriptionQueueItem>> getQueue() {
        return ResponseEntity.ok(processingService.getQueue());
    }

    // Module 5: Prescription Tracking page - a patient looking up their own prescriptions.
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getByPatient(@PathVariable String patientId,
                                                           @AuthenticationPrincipal User user,
                                                           Authentication authentication) {
        boolean patient = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"));
        if (patient && !patientId.equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Patients may only view their own prescriptions");
        }
        return ResponseEntity.ok(processingService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Prescription>> getByDoctor(@PathVariable String doctorId,
                                                          @AuthenticationPrincipal User user,
                                                          Authentication authentication) {
        boolean doctor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DOCTOR"));
        if (doctor && !doctorId.equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Doctors may only view their own prescriptions");
        }
        return ResponseEntity.ok(processingService.getByDoctor(doctorId));
    }

    @GetMapping("/ready-for-collection")
    public ResponseEntity<List<Prescription>> getReadyForCollection() {
        return ResponseEntity.ok(processingService.getReadyForCollection());
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<Prescription> track(@PathVariable String prescriptionId,
                                              @AuthenticationPrincipal User user,
                                              Authentication authentication) {
        Prescription prescription = processingService.getById(prescriptionId);
        boolean patient = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"));
        boolean doctor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DOCTOR"));
        if (patient && !prescription.getPatientId().equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Patients may only view their own prescriptions");
        }
        if (doctor && !prescription.getDoctorId().equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Doctors may only view their own prescriptions");
        }
        return ResponseEntity.ok(prescription);
    }

    // Advances Pending -> Received -> Preparing -> Ready for Collection -> Collected,
    // one step per call, and fires the patient notification automatically.
    @PutMapping("/{prescriptionId}/advance")
    public ResponseEntity<Prescription> advanceStatus(@PathVariable String prescriptionId) {
        return ResponseEntity.ok(processingService.advanceStatus(prescriptionId));
    }

    public static class PrescriptionStatusRequest {
        public PrescriptionStatus status;
    }

    @PutMapping("/{prescriptionId}/status")
    public ResponseEntity<Prescription> updateClinicalStatus(@PathVariable String prescriptionId,
                                                              @RequestBody PrescriptionStatusRequest request) {
        return ResponseEntity.ok(processingService.updateClinicalStatus(prescriptionId, request.status));
    }
}