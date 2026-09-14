package za.ac.cput.medisnyc.dto;

/* CreateDoctorRequest.java
   Module 2/6: request body for an admin adding a doctor to the practice.
*/

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateDoctorRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String specialization;

    @NotBlank
    private String phoneNumber;

    @NotBlank @Email
    private String email;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
