package za.ac.cput.medisnyc.domain;

/* Appointment.java
   Appointment entity - Module 2: Patient & Appointment Module.
   Author: Lisakhanya Mpahla
*/

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime appointmentDate;
    private String reason;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    private String notes;

    protected Appointment() {
    }

    private Appointment(Builder builder) {
        this.appointmentId = builder.appointmentId;
        this.patientId = builder.patientId;
        this.doctorId = builder.doctorId;
        this.appointmentDate = builder.appointmentDate;
        this.reason = builder.reason;
        this.status = builder.status != null ? builder.status : AppointmentStatus.SCHEDULED;
        this.notes = builder.notes;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }

    public boolean canBeCancelled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(appointmentId, that.appointmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", appointmentDate=" + appointmentDate +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                '}';
    }

    public static class Builder {
        private String appointmentId;
        private String patientId;
        private String doctorId;
        private LocalDateTime appointmentDate;
        private String reason;
        private AppointmentStatus status;
        private String notes;

        public Builder setAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder setPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder setDoctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder setAppointmentDate(LocalDateTime appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public Builder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder setStatus(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder copy(Appointment appointment) {
            this.appointmentId = appointment.appointmentId;
            this.patientId = appointment.patientId;
            this.doctorId = appointment.doctorId;
            this.appointmentDate = appointment.appointmentDate;
            this.reason = appointment.reason;
            this.status = appointment.status;
            this.notes = appointment.notes;
            return this;
        }

        public Appointment build() {
            if (appointmentId == null || appointmentId.isBlank()) {
                throw new IllegalArgumentException("Appointment ID is required");
            }
            if (patientId == null || patientId.isBlank()) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            if (doctorId == null || doctorId.isBlank()) {
                throw new IllegalArgumentException("Doctor ID is required");
            }
            if (appointmentDate == null) {
                throw new IllegalArgumentException("Appointment date is required");
            }
            return new Appointment(this);
        }
    }
}
