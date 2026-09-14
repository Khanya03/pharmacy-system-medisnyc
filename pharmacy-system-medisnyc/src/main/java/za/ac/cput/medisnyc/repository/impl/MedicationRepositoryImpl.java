package za.ac.cput.medisnyc.repository.impl;

/* MedicationRepositoryImpl.java
   Medication repository implementation
   Author: Lukhanyo Mweli 222830646
   Date: 23 March 2026
*/

import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.repository.MedicationRepository;

import java.util.HashSet;
import java.util.Set;

public class MedicationRepositoryImpl implements MedicationRepository {

    private static MedicationRepository repository = null;
    private Set<Medication> medicationDB;

    private MedicationRepositoryImpl() {
        medicationDB = new HashSet<>();
    }

    public static MedicationRepository getRepository() {
        if (repository == null) {
            repository = new MedicationRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Medication create(Medication medication) {
        medicationDB.add(medication);
        return medication;
    }

    @Override
    public Medication read(String medicationId) {
        return medicationDB.stream()
                .filter(m -> m.getMedicationId().equals(medicationId))
                .findAny()
                .orElse(null);
    }

    @Override
    public Medication update(Medication medication) {
        Medication oldMedication = read(medication.getMedicationId());
        if (oldMedication != null) {
            medicationDB.remove(oldMedication);
            medicationDB.add(medication);
            return medication;
        }
        return null;
    }

    @Override
    public boolean delete(String medicationId) {
        Medication medication = read(medicationId);
        if (medication != null) {
            medicationDB.remove(medication);
            return true;
        }
        return false;
    }

    @Override
    public Set<Medication> getAll() {
        return medicationDB;
    }
}