package za.ac.cput.medisnyc.factory;

/* MedicationFactory.java
   Medication factory class
   Author: Lukhanyo Mweli 222830646
   Date: 15 March 2026
*/

import za.ac.cput.medisnyc.domain.Medication;

public class MedicationFactory {

    public static Medication createMedication(String medicationId,
                                              String medicationName,
                                              String dosageForm,
                                              String manufacturer,
                                              String strength,
                                              String description,
                                              String category) {

        if (medicationId == null || medicationId.isBlank() ||
                medicationName == null || medicationName.isBlank() ||
                dosageForm == null || dosageForm.isBlank() ||
                manufacturer == null || manufacturer.isBlank()) {
            return null;
        }

        return new Medication.Builder()
                .setMedicationId(medicationId)
                .setMedicationName(medicationName)
                .setDosageForm(dosageForm)
                .setManufacturer(manufacturer)
                .setStrength(strength)
                .setDescription(description)
                .setCategory(category)
                .build();
    }
}