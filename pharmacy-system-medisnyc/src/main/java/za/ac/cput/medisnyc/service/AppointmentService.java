package za.ac.cput.medisnyc.service;

/* AppointmentService.java
   Module 2: Patient & Appointment Module.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.domain.Appointment;
import za.ac.cput.medisnyc.domain.AppointmentStatus;
import za.ac.cput.medisnyc.dto.CreateAppointmentRequest;
import za.ac.cput.medisnyc.repository.jpa.AppointmentJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.DoctorJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentJpaRepository appointmentRepository;
    private final DoctorJpaRepository doctorRepository;

    @Autowired
    public AppointmentService(AppointmentJpaRepository appointmentRepository, DoctorJpaRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Appointment bookAppointment(CreateAppointmentRequest request) {
        if (!doctorRepository.existsById(request.getDoctorId())) {
            throw new IllegalArgumentException("Doctor not found: " + request.getDoctorId());
        }

        Appointment appointment = new Appointment.Builder()
                .setAppointmentId("APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .setPatientId(request.getPatientId())
                .setDoctorId(request.getDoctorId())
                .setAppointmentDate(request.getAppointmentDate())
                .setReason(request.getReason())
                .setNotes(request.getNotes())
                .setStatus(AppointmentStatus.SCHEDULED)
                .build();

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getByDoctor(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    public Appointment getById(String appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
    }

    @Transactional
    public Appointment updateStatus(String appointmentId, AppointmentStatus status) {
        Appointment existing = getById(appointmentId);
        Appointment updated = new Appointment.Builder().copy(existing).setStatus(status).build();
        return appointmentRepository.save(updated);
    }

    @Transactional
    public Appointment cancelAppointment(String appointmentId) {
        Appointment existing = getById(appointmentId);
        if (!existing.canBeCancelled()) {
            throw new IllegalStateException("Appointment cannot be cancelled in its current status: " + existing.getStatus());
        }
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
}