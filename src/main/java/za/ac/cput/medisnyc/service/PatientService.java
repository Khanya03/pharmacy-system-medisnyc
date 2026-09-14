package za.ac.cput.medisnyc.service;

/* PatientService.java
   Module 6/1: patient directory - lookup for admin views, and creation
   either by an admin (walk-in patient) or automatically at self-registration.
   Mirrors DoctorService for consistency.
*/

import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.factory.PatientFactory;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    private final PatientJpaRepository patientRepository;

    @Autowired
    public PatientService(PatientJpaRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    public Patient getById(String medicalId) {
        return patientRepository.findById(medicalId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + medicalId));
    }

    public Patient create(String firstName, String lastName, String email,
                          String phoneNumber, LocalDate dateOfBirth, List<String> allergies) {
        String medicalId = Helper.generateId("PAT");
        Patient patient = PatientFactory.createPatient(
                medicalId, firstName, lastName, email, phoneNumber, dateOfBirth, allergies);
        return patientRepository.save(patient);
    }
}
