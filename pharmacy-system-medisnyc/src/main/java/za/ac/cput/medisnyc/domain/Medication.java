package za.ac.cput.medisnyc.domain;

/* Medication.java
   Medication model class
   Author: Lukhanyo Mweli 222830646
   Date: 15 March 2026
*/

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    private String medicationId;
    private String medicationName;
    private String dosageForm;
    private String manufacturer;
    private String strength;
    private String description;
    private String category;

    protected Medication() {
    }

    private Medication(Builder builder) {
        this.medicationId = builder.medicationId;
        this.medicationName = builder.medicationName;
        this.dosageForm = builder.dosageForm;
        this.manufacturer = builder.manufacturer;
        this.strength = builder.strength;
        this.description = builder.description;
        this.category = builder.category;
    }

    public String getMedicationId() { return medicationId; }
    public String getMedicationName() { return medicationName; }
    public String getDosageForm() { return dosageForm; }
    public String getManufacturer() { return manufacturer; }
    public String getStrength() { return strength; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return "Medication{" +
                "medicationId='" + medicationId + '\'' +
                ", medicationName='" + medicationName + '\'' +
                ", dosageForm='" + dosageForm + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", strength='" + strength + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public static class Builder {
        private String medicationId;
        private String medicationName;
        private String dosageForm;
        private String manufacturer;
        private String strength;
        private String description;
        private String category;

        public Builder setMedicationId(String medicationId) {
            this.medicationId = medicationId;
            return this;
        }
        public Builder setMedicationName(String medicationName) {
            this.medicationName = medicationName;
            return this;
        }
        public Builder setDosageForm(String dosageForm) {
            this.dosageForm = dosageForm;
            return this;
        }
        public Builder setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }
        public Builder setStrength(String strength) {
            this.strength = strength;
            return this;
        }
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }
        public Builder copy(Medication medication) {
            this.medicationId = medication.medicationId;
            this.medicationName = medication.medicationName;
            this.dosageForm = medication.dosageForm;
            this.manufacturer = medication.manufacturer;
            this.strength = medication.strength;
            this.description = medication.description;
            this.category = medication.category;
            return this;
        }
        public Medication build() {
            return new Medication(this);
        }
    }
}