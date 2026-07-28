package za.ac.cput.medisnyc.repository.impl;

/* PatientRepositoryImpl.java
   Patient Repository implementation
   Author:Siphesihle Mposelwa
   Student Number: 222330325
   Date: 19 March 2026
*/

import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.repository.PatientRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientRepositoryImpl implements PatientRepository {

    private final Map<String, Patient> patientMap = new HashMap<>();
    private static PatientRepositoryImpl repository = null;

    private PatientRepositoryImpl() {}

    public static PatientRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new PatientRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Patient create(Patient patient) {
        if (patient == null) return null;
        patientMap.put(patient.getMedicalId(), patient);
        return patient;
    }

    @Override
    public Patient read(String medicalId) {
        return patientMap.get(medicalId);
    }

    @Override
    public Patient update(Patient patient) {
        if (patient == null || !patientMap.containsKey(patient.getMedicalId())) {
            return null;
        }
        patientMap.put(patient.getMedicalId(), patient);
        return patient;
    }

    @Override
    public boolean delete(String medicalId) {
        return patientMap.remove(medicalId) != null;
    }

    @Override
    public List<Patient> getAll() {
        return new ArrayList<>(patientMap.values());
    }

    @Override
    public boolean existsByMedicalId(String medicalId) {
        return patientMap.containsKey(medicalId);
    }

    @Override
    public Patient findByEmail(String email) {
        return patientMap.values().stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Patient> findByLastName(String lastName) {
        return patientMap.values().stream()
                .filter(p -> p.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Patient> findPatientsWithAllergies(String allergy) {
        return patientMap.values().stream()
                .filter(p -> p.hasAllergy(allergy))
                .collect(Collectors.toList());
    }
}