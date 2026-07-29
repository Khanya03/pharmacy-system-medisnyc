package za.ac.cput.medisnyc.controller;

/* AppointmentController.java
   Module 2: Patient & Appointment Module.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Appointment;
import za.ac.cput.medisnyc.domain.AppointmentStatus;
import za.ac.cput.medisnyc.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getByDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getById(@PathVariable String appointmentId) {
        return ResponseEntity.ok(appointmentService.getById(appointmentId));
    }

    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<Appointment> updateStatus(@PathVariable String appointmentId,
                                                    @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, status));
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable String appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
    }
}
