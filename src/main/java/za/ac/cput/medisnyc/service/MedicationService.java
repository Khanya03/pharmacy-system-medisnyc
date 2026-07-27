package za.ac.cput.medisnyc.service;

/* MedicationService.java
   Module 4: Pharmacy Inventory Module (medicine catalogue).
   Author: Lukhanyo
*/

import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicationService {

    private final MedicationJpaRepository medicationRepository;

    @Autowired
    public MedicationService(MedicationJpaRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAll() {
        return medicationRepository.findAll();
    }

    public Medication getById(String medicationId) {
        return medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found: " + medicationId));
    }

    @Transactional
    public Medication addMedicine(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Transactional
    public Medication updateMedicine(String medicationId, Medication medication) {
        getById(medicationId); // ensures it exists, 404 otherwise
        return medicationRepository.save(medication);
    }

    @Transactional
    public void deleteMedicine(String medicationId) {
        getById(medicationId);
        medicationRepository.deleteById(medicationId);
    }

    public List<Medication> search(String name) {
        return medicationRepository.findByMedicationNameContainingIgnoreCase(name);
    }
}