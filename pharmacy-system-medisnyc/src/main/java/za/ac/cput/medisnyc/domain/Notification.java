package za.ac.cput.medisnyc.domain;

/* Notification.java
   Notification entity - Module 5: Prescription Processing Module.
   Author: Notification
*/

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String patientId;
    private String prescriptionId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    protected Notification() {
    }

    private Notification(Builder builder) {
        this.notificationId = builder.notificationId;
        this.patientId = builder.patientId;
        this.prescriptionId = builder.prescriptionId;
        this.message = builder.message;
        this.isRead = builder.isRead;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    public Long getNotificationId() { return notificationId; }
    public String getPatientId() { return patientId; }
    public String getPrescriptionId() { return prescriptionId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", patientId='" + patientId + '\'' +
                ", prescriptionId='" + prescriptionId + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    public static class Builder {
        private Long notificationId;
        private String patientId;
        private String prescriptionId;
        private String message;
        private boolean isRead = false;
        private LocalDateTime createdAt;

        public Builder setNotificationId(Long notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder setPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setRead(boolean read) {
            isRead = read;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Notification build() {
            if (patientId == null || patientId.isBlank()) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Message is required");
            }
            return new Notification(this);
        }
    }
}