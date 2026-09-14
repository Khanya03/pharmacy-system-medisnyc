package za.ac.cput.medisnyc.repository.impl;

/* DoctorRepositoryImpl.java
   Doctor Repository implementation
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.repository.DoctorRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorRepositoryImpl implements DoctorRepository {

    private final Map<String, Doctor> doctorMap = new HashMap<>();
    private static DoctorRepositoryImpl repository = null;

    private DoctorRepositoryImpl() {}

    public static DoctorRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new DoctorRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Doctor create(Doctor doctor) {
        if (doctor == null) return null;
        doctorMap.put(doctor.getDoctorId(), doctor);
        return doctor;
    }

    @Override
    public Doctor read(String doctorId) {
        return doctorMap.get(doctorId);
    }

    @Override
    public Doctor update(Doctor doctor) {
        if (doctor == null || !doctorMap.containsKey(doctor.getDoctorId())) {
            return null;
        }
        doctorMap.put(doctor.getDoctorId(), doctor);
        return doctor;
    }

    @Override
    public boolean delete(String doctorId) {
        return doctorMap.remove(doctorId) != null;
    }

    @Override
    public List<Doctor> getAll() {
        return new ArrayList<>(doctorMap.values());
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) {
        if (specialization == null) return new ArrayList<>();
        return doctorMap.values().stream()
                .filter(doctor -> specialization.equalsIgnoreCase(doctor.getSpecialization()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> findByLastName(String lastName) {
        if (lastName == null) return new ArrayList<>();
        return doctorMap.values().stream()
                .filter(doctor -> lastName.equalsIgnoreCase(doctor.getLastName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> findByFirstName(String firstName) {
        if (firstName == null) return new ArrayList<>();
        return doctorMap.values().stream()
                .filter(doctor -> firstName.equalsIgnoreCase(doctor.getFirstName()))
                .collect(Collectors.toList());
    }

    // Additional helper method for tests
    public boolean existsById(String doctorId) {
        return doctorMap.containsKey(doctorId);
    }

    public void clear() {
        doctorMap.clear();
    }
}