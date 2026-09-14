package za.ac.cput.medisnyc.controller;

/* DoctorController.java
   Module 2: doctor directory - read-only lookup used by the patient-facing
   booking flow, plus creation (admin only) so a practice can actually have
   doctors to book with.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.dto.CreateDoctorRequest;
import za.ac.cput.medisnyc.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(doctorService.getAll());
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<Doctor> getById(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.getById(doctorId));
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@Valid @RequestBody CreateDoctorRequest request) {
        Doctor created = doctorService.create(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecialization(),
                request.getPhoneNumber(),
                request.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
