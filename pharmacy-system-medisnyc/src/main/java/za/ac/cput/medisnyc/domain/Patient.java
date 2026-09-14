package za.ac.cput.medisnyc.domain;

/* Patient.java
   Patient model class
   Author:Siphesihle Mposelwa
   Student Number: 222330325
   Date: 19 March 2026
*/



import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    private String medicalId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    @ElementCollection
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy")
    private List<String> allergies;

    protected Patient() {
    }

    private Patient(Builder builder) {
        this.medicalId = builder.medicalId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.dateOfBirth = builder.dateOfBirth;
        this.allergies = builder.allergies != null ?
                new ArrayList<>(builder.allergies) : new ArrayList<>();
    }


    public String getMedicalId() { return medicalId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public List<String> getAllergies() {
        return Collections.unmodifiableList(allergies);
    }

    public boolean hasAllergy(String allergy) {
        return allergies.contains(allergy.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(medicalId, patient.medicalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalId);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "medicalId='" + medicalId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", allergies=" + allergies +
                '}';
    }


    public static class Builder {
        private String medicalId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private List<String> allergies = new ArrayList<>();

        public Builder setMedicalId(String medicalId) {
            this.medicalId = medicalId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder setAllergies(List<String> allergies) {
            this.allergies = allergies;
            return this;
        }

        public Builder addAllergy(String allergy) {
            if (this.allergies == null) {
                this.allergies = new ArrayList<>();
            }
            this.allergies.add(allergy.toLowerCase());
            return this;
        }

        public Patient build() {
            return new Patient(this);
        }
    }
}
