package za.ac.cput.factory;



/* PatientFactory.java
   Patient factory class
   Author:Siphesihle Mposelwa
   Student Number: 222330325
   Date:19 March 2026
*/


import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.util.Helper;

import java.time.LocalDate;
import java.util.List;

public class PatientFactory {

    public static Patient createPatient(String medicalId, String firstName,
                                        String lastName, String email,
                                        String phoneNumber, LocalDate dateOfBirth,
                                        List<String> allergies) {

        if (Helper.isNullOrEmpty(medicalId)) {
            throw new IllegalArgumentException("Medical ID cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(firstName)) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(lastName)) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (!Helper.isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (Helper.isNullOrEmpty(phoneNumber)) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Valid date of birth is required");
        }

        return new Patient.Builder()
                .setMedicalId(medicalId.trim().toUpperCase())
                .setFirstName(Helper.capitalizeFirstLetter(firstName.trim()))
                .setLastName(Helper.capitalizeFirstLetter(lastName.trim()))
                .setEmail(email.trim().toLowerCase())
                .setPhoneNumber(phoneNumber.trim())
                .setDateOfBirth(dateOfBirth)
                .setAllergies(allergies)
                .build();
    }

    public static Patient createPatient(String medicalId, String firstName,
                                        String lastName, String email,
                                        String phoneNumber, LocalDate dateOfBirth) {
        return createPatient(medicalId, firstName, lastName, email,
                phoneNumber, dateOfBirth, null);
    }
}