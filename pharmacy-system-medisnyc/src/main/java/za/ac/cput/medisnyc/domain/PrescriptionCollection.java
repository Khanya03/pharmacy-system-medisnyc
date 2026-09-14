package za.ac.cput.medisnyc.domain;

/* PrescriptionCollection.java
   Collection entity - Module 5: records when/by-whom a ready prescription
   was actually collected from the pharmacy.
   (Named PrescriptionCollection rather than "Collection" to avoid clashing
   with java.util.Collection.)
   Author: Naledi Ngobeni
*/

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "prescription_collections")
public class PrescriptionCollection {

    @Id
    private String collectionId;
    private String prescriptionId;
    private String patientId;
    private String collectedBy; // pharmacist ID who handed it over
    private LocalDateTime collectedAt;
    private String collectionNotes;

    protected PrescriptionCollection() {
    }

    private PrescriptionCollection(Builder builder) {
        this.collectionId = builder.collectionId;
        this.prescriptionId = builder.prescriptionId;
        this.patientId = builder.patientId;
        this.collectedBy = builder.collectedBy;
        this.collectedAt = builder.collectedAt != null ? builder.collectedAt : LocalDateTime.now();
        this.collectionNotes = builder.collectionNotes;
    }

    public String getCollectionId() { return collectionId; }
    public String getPrescriptionId() { return prescriptionId; }
    public String getPatientId() { return patientId; }
    public String getCollectedBy() { return collectedBy; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public String getCollectionNotes() { return collectionNotes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptionCollection that = (PrescriptionCollection) o;
        return Objects.equals(collectionId, that.collectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId);
    }

    @Override
    public String toString() {
        return "PrescriptionCollection{" +
                "collectionId='" + collectionId + '\'' +
                ", prescriptionId='" + prescriptionId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", collectedBy='" + collectedBy + '\'' +
                ", collectedAt=" + collectedAt +
                ", collectionNotes='" + collectionNotes + '\'' +
                '}';
    }

    public static class Builder {
        private String collectionId;
        private String prescriptionId;
        private String patientId;
        private String collectedBy;
        private LocalDateTime collectedAt;
        private String collectionNotes;

        public Builder setCollectionId(String collectionId) {
            this.collectionId = collectionId;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder setCollectedBy(String collectedBy) {
            this.collectedBy = collectedBy;
            return this;
        }

        public Builder setCollectedAt(LocalDateTime collectedAt) {
            this.collectedAt = collectedAt;
            return this;
        }

        public Builder setCollectionNotes(String collectionNotes) {
            this.collectionNotes = collectionNotes;
            return this;
        }

        public PrescriptionCollection build() {
            if (collectionId == null || collectionId.isBlank()) {
                throw new IllegalArgumentException("Collection ID is required");
            }
            if (prescriptionId == null || prescriptionId.isBlank()) {
                throw new IllegalArgumentException("Prescription ID is required");
            }
            return new PrescriptionCollection(this);
        }
    }
}