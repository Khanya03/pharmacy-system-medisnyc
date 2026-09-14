package za.ac.cput.medisnyc.domain;

/* Prescription.java
   Prescription model class
   Author: Naledi Ngobeni (230742912)
   Date: 20 March 2026
*/

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id
    private String prescriptionId;
    private String patientId;
    private String doctorId;
    private LocalDate dateIssued;
    private LocalDate expiryDate;
    private String instructions;
    private int refillsAllowed;
    private int refillsUsed;
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    // Module 5: Pharmacy processing/collection workflow status,
    // separate from the clinical status (ACTIVE/EXPIRED/CANCELLED) above.
    @Enumerated(EnumType.STRING)
    private PrescriptionProcessingStatus processingStatus;

    protected Prescription() {
    }

    private Prescription(Builder builder) {
        this.prescriptionId = builder.prescriptionId;
        this.patientId = builder.patientId;
        this.doctorId = builder.doctorId;
        this.dateIssued = builder.dateIssued;
        this.expiryDate = builder.expiryDate;
        this.instructions = builder.instructions;
        this.refillsAllowed = builder.refillsAllowed;
        this.refillsUsed = builder.refillsUsed;
        this.status = builder.status;
        this.processingStatus = builder.processingStatus != null ?
                builder.processingStatus : PrescriptionProcessingStatus.PENDING;
    }

    public PrescriptionProcessingStatus getProcessingStatus() {
        return processingStatus;
    }


    public String getPrescriptionId() {
        return prescriptionId; }
    public String getPatientId() {
        return patientId; }
    public String getDoctorId() {
        return doctorId; }
    public LocalDate getDateIssued() {
        return dateIssued; }
    public LocalDate getExpiryDate() {
        return expiryDate; }
    public String getInstructions() {
        return instructions; }
    public int getRefillsAllowed() {
        return refillsAllowed; }
    public int getRefillsUsed() {
        return refillsUsed; }
    public PrescriptionStatus getStatus() {
        return status; }

    public boolean canBeFilled() {
        return status == PrescriptionStatus.ACTIVE
                && !LocalDate.now().isAfter(expiryDate)
                && refillsUsed < refillsAllowed;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId='" + prescriptionId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", dateIssued=" + dateIssued +
                ", expiryDate=" + expiryDate +
                ", instructions='" + instructions + '\'' +
                ", refillsAllowed=" + refillsAllowed +
                ", refillsUsed=" + refillsUsed +
                ", status=" + status +
                '}';
    }

    public static class Builder {
        private String prescriptionId;
        private String patientId;
        private String doctorId;
        private LocalDate dateIssued;
        private LocalDate expiryDate;
        private String instructions;
        private int refillsAllowed;
        private int refillsUsed;
        private PrescriptionStatus status;
        private PrescriptionProcessingStatus processingStatus;

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
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

        public Builder setDateIssued(LocalDate dateIssued) {
            this.dateIssued = dateIssued;
            return this;
        }

        public Builder setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder setInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder setRefillsAllowed(int refillsAllowed) {
            this.refillsAllowed = refillsAllowed;
            return this;
        }

        public Builder setRefillsUsed(int refillsUsed) {
            this.refillsUsed = refillsUsed;
            return this;
        }

        public Builder setStatus(PrescriptionStatus status) {
            this.status = status;
            return this;
        }

        public Builder setProcessingStatus(PrescriptionProcessingStatus processingStatus) {
            this.processingStatus = processingStatus;
            return this;
        }

        public Prescription build() {
            return new Prescription(this);
        }
    }
}