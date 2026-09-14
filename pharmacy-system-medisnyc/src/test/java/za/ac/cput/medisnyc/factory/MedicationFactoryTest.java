package za.ac.cput.medisnyc.factory;

/* MedicationFactoryTest.java
   Medication factory test class
   Author: Lukhanyo Mweli 222830646
   Date: 18 March 2026
*/

import org.junit.Test;
import za.ac.cput.medisnyc.domain.Medication;
import static org.junit.Assert.*;

public class MedicationFactoryTest {

    @Test
    public void testCreateSuccess() {
        Medication medication = MedicationFactory.createMedication(
                "M001", "Panado", "Tablet",
                "Adcock Ingram", "500mg", "Pain relief", "Analgesic"
        );
        assertNotNull(medication);
        assertEquals("M001", medication.getMedicationId());
        assertEquals("Panado", medication.getMedicationName());
    }

    @Test
    public void testCreateFailsWithNullId() {
        Medication medication = MedicationFactory.createMedication(
                null, "Panado", "Tablet",
                "Adcock Ingram", "500mg", "Pain relief", "Analgesic"
        );
        assertNull(medication);
    }

    @Test
    public void testCreateFailsWithBlankName() {
        Medication medication = MedicationFactory.createMedication(
                "M001", "  ", "Tablet",
                "Adcock Ingram", "500mg", "Pain relief", "Analgesic"
        );
        assertNull(medication);
    }
}