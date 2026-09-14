package za.ac.cput.medisnyc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    // e.g. "PATIENT", "DOCTOR", "PHARMACIST", "ADMIN"
    @NotBlank
    private String role;

    // Optional: links to an existing Patient.medicalId or Doctor.doctorId
    private String linkedProfileId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getLinkedProfileId() { return linkedProfileId; }
    public void setLinkedProfileId(String linkedProfileId) { this.linkedProfileId = linkedProfileId; }
}
