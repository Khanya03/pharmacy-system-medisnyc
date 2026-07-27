package za.ac.cput.medisnyc.repository.impl;

/* PrescriptionRepositoryImpl.java
   Prescription factory class
   Author: Naledi Ngobeni (230742912)
   Date: 16 March 2026
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.repository.PrescriptionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionRepositoryImpl implements PrescriptionRepository {

    private static Map<String, Prescription> prescriptions = new HashMap<>();
    private static PrescriptionRepositoryImpl instance = null;

    private PrescriptionRepositoryImpl() {}

    public static PrescriptionRepositoryImpl getRepository() {
        if (instance == null) {
            instance = new PrescriptionRepositoryImpl();
        }
        return instance;
    }

    @Override
    public Prescription create(Prescription prescription) {
        if (prescription == null) return null;
        prescriptions.put(prescription.getPrescriptionId(), prescription);
        return prescription;
    }

    @Override
    public Prescription read(String id) {
        return prescriptions.get(id);
    }

    @Override
    public Prescription update(Prescription prescription) {
        if (prescription == null) return null;
        if (!prescriptions.containsKey(prescription.getPrescriptionId())) return null;
        prescriptions.put(prescription.getPrescriptionId(), prescription);
        return prescription;
    }

    @Override
    public boolean delete(String id) {
        return prescriptions.remove(id) != null;
    }

    @Override
    public List<Prescription> getAll() {
        return new ArrayList<>(prescriptions.values());
    }

    @Override
    public List<Prescription> findByPatientId(String patientId) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription p : prescriptions.values()) {
            if (p.getPatientId().equals(patientId)) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Prescription> findByStatus(PrescriptionStatus status) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription p : prescriptions.values()) {
            if (p.getStatus() == status) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Prescription> findActivePrescriptions(String patientId) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription p : prescriptions.values()) {
            if (p.getPatientId().equals(patientId) && p.canBeFilled()) {
                result.add(p);
            }
        }
        return result;
    }
}