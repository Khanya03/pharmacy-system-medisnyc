package za.ac.cput.medisnyc.dto;

/* CreateAppointmentRequest.java
   Module 2: request body for a patient booking an appointment with a doctor.
   Appointment is a JPA entity built only via its Builder (no setters), so it
   can't be deserialized directly from JSON - this DTO is the mutable shape
   Jackson binds to, mirroring CreateDoctorRequest.
   Author: Lisakhanya Mpahla
*/

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateAppointmentRequest {

    @NotBlank
    private String patientId;

    @NotBlank
    private String doctorId;

    @NotNull
    private LocalDateTime appointmentDate;

    private String reason;

    private String notes;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
