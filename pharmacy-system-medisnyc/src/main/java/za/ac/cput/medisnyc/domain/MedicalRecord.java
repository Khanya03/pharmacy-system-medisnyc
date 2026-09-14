package za.ac.cput.medisnyc.domain;

/* MedicalRecord.java
   MedicalRecord entity - Module 3: Doctor Consultation & Prescription Module.
   Author: Lukhanyo Mweli 222830646
*/

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    private String recordId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private LocalDateTime visitDate;
    private String diagnosis;
    private String consultationNotes;
    private String treatmentPlan;

    protected MedicalRecord() {
    }

    private MedicalRecord(Builder builder) {
        this.recordId = builder.recordId;
        this.patientId = builder.patientId;
        this.doctorId = builder.doctorId;
        this.appointmentId = builder.appointmentId;
        this.visitDate = builder.visitDate != null ? builder.visitDate : LocalDateTime.now();
        this.diagnosis = builder.diagnosis;
        this.consultationNotes = builder.consultationNotes;
        this.treatmentPlan = builder.treatmentPlan;
    }

    public String getRecordId() { return recordId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public String getDiagnosis() { return diagnosis; }
    public String getConsultationNotes() { return consultationNotes; }
    public String getTreatmentPlan() { return treatmentPlan; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "recordId='" + recordId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", appointmentId='" + appointmentId + '\'' +
                ", visitDate=" + visitDate +
                ", diagnosis='" + diagnosis + '\'' +
                ", consultationNotes='" + consultationNotes + '\'' +
                ", treatmentPlan='" + treatmentPlan + '\'' +
                '}';
    }

    public static class Builder {
        private String recordId;
        private String patientId;
        private String doctorId;
        private String appointmentId;
        private LocalDateTime visitDate;
        private String diagnosis;
        private String consultationNotes;
        private String treatmentPlan;

        public Builder setRecordId(String recordId) {
            this.recordId = recordId;
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

        public Builder setAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder setVisitDate(LocalDateTime visitDate) {
            this.visitDate = visitDate;
            return this;
        }

        public Builder setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
            return this;
        }

        public Builder setConsultationNotes(String consultationNotes) {
            this.consultationNotes = consultationNotes;
            return this;
        }

        public Builder setTreatmentPlan(String treatmentPlan) {
            this.treatmentPlan = treatmentPlan;
            return this;
        }

        public Builder copy(MedicalRecord record) {
            this.recordId = record.recordId;
            this.patientId = record.patientId;
            this.doctorId = record.doctorId;
            this.appointmentId = record.appointmentId;
            this.visitDate = record.visitDate;
            this.diagnosis = record.diagnosis;
            this.consultationNotes = record.consultationNotes;
            this.treatmentPlan = record.treatmentPlan;
            return this;
        }

        public MedicalRecord build() {
            if (recordId == null || recordId.isBlank()) {
                throw new IllegalArgumentException("Record ID is required");
            }
            if (patientId == null || patientId.isBlank()) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            if (doctorId == null || doctorId.isBlank()) {
                throw new IllegalArgumentException("Doctor ID is required");
            }
            return new MedicalRecord(this);
        }
    }
}