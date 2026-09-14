/* Domain.java
   Utility Domain class
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

package za.ac.cput.medisnyc.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    private String doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String phoneNumber;
    private String email;

    protected Doctor() {
    }

    private Doctor(Builder builder) {
        this.doctorId = builder.doctorId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.specialization = builder.specialization;
        this.phoneNumber = builder.phoneNumber;
        this.email = builder.email;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId='" + doctorId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public static class Builder {
        private String doctorId;
        private String firstName;
        private String lastName;
        private String specialization;
        private String phoneNumber;
        private String email;

        public Builder setDoctorId(String doctorId) {
            this.doctorId = doctorId;
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

        public Builder setSpecialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public static Builder copy(Doctor doctor) {
            Builder builder = new Builder();
            builder.doctorId = doctor.doctorId;
            builder.firstName = doctor.firstName;
            builder.lastName = doctor.lastName;
            builder.specialization = doctor.specialization;
            builder.phoneNumber = doctor.phoneNumber;
            builder.email = doctor.email;
            return builder;
        }

        public Doctor build() {
            return new Doctor(this);
        }
    }
}