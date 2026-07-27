package za.ac.cput.medisnyc.repository;

/* MedicationRepository.java
   Medication repository interface
   Author: Lukhanyo Mweli 222830646
   Date: 23 March 2026
*/


import za.ac.cput.medisnyc.domain.Medication;
import java.util.Set;

public interface MedicationRepository {

    Medication create(Medication medication);

    Medication read(String medicationId);

    Medication update(Medication medication);

    boolean delete(String medicationId);

    Set<Medication> getAll();
}