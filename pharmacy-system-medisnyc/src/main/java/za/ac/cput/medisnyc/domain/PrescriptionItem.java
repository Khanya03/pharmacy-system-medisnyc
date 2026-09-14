package za.ac.cput.medisnyc.domain;

/* PrescriptionItem.java
   PrescriptionItem model class
   Author: Naledi Ngobeni (230742912)
   Date: 20 March 2026
*/

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {
    @Id
    private String prescriptionItemId;
    private String prescriptionId;
    private String medicationId;
    private int quantity;
    private String dosage;
    private String frequency;
    private int durationDays;

    protected PrescriptionItem() {
    }

    private PrescriptionItem(Builder builder) {
        this.prescriptionItemId = builder.prescriptionItemId;
        this.prescriptionId = builder.prescriptionId;
        this.medicationId = builder.medicationId;
        this.quantity = builder.quantity;
        this.dosage = builder.dosage;
        this.frequency = builder.frequency;
        this.durationDays = builder.durationDays;
    }


    public String getPrescriptionItemId()
    { return prescriptionItemId; }
    public String getPrescriptionId() {
        return prescriptionId; }
    public String getMedicationId() {
        return medicationId; }
    public int getQuantity() {
        return quantity; }
    public String getDosage() {
        return dosage; }
    public String getFrequency() {
        return frequency; }
    public int getDurationDays() {
        return durationDays; }

    @Override
    public String toString() {
        return "PrescriptionItem{" +
                "prescriptionItemId='" + prescriptionItemId + '\'' +
                ", prescriptionId='" + prescriptionId + '\'' +
                ", medicationId='" + medicationId + '\'' +
                ", quantity=" + quantity +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", durationDays=" + durationDays +
                '}';
    }

    public static class Builder {
        private String prescriptionItemId;
        private String prescriptionId;
        private String medicationId;
        private int quantity;
        private String dosage;
        private String frequency;
        private int durationDays;

        public Builder setPrescriptionItemId(String prescriptionItemId) {
            this.prescriptionItemId = prescriptionItemId;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setMedicationId(String medicationId) {
            this.medicationId = medicationId;
            return this;
        }

        public Builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setDosage(String dosage) {
            this.dosage = dosage;
            return this;
        }

        public Builder setFrequency(String frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setDurationDays(int durationDays) {
            this.durationDays = durationDays;
            return this;
        }

        public PrescriptionItem build() {
            return new PrescriptionItem(this);
        }
    }
}