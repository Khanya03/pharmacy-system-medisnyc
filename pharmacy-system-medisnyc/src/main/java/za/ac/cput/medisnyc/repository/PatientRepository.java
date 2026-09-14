package za.ac.cput.medisnyc.repository;

/* PatientRepository.java
PatientRepository class
Author:Siphesihle Mposelwa
Student Number: 222330325
Date:19 March 2025
 */


import za.ac.cput.medisnyc.domain.Patient;
import java.util.List;

public interface PatientRepository {


    Patient create(Patient patient);
    Patient read(String medicalId);
    Patient update(Patient patient);
    boolean delete(String medicalId);
    List<Patient> getAll();


    Patient findByEmail(String email);
    List<Patient> findByLastName(String lastName);
    List<Patient> findPatientsWithAllergies(String allergy);
    boolean existsByMedicalId(String medicalId);
}