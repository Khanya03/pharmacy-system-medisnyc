package za.ac.cput.medisnyc.repository;

/* MedicationRepositoryTest.java
   Medication repository test class
   Author: Lukhanyo Mweli 222830646
   Date: 24 March 2026
*/

import org.junit.Before;
import org.junit.Test;
import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.factory.MedicationFactory;
import za.ac.cput.medisnyc.repository.impl.MedicationRepositoryImpl;
import static org.junit.Assert.*;

public class MedicationRepositoryTest {

    private MedicationRepository repository = MedicationRepositoryImpl.getRepository();
    private Medication medication;

    @Before
    public void setUp() {
        medication = MedicationFactory.createMedication(
                "M001", "Panado", "Tablet",
                "Adcock Ingram", "500mg", "Pain relief", "Analgesic"
        );
    }

    @Test
    public void testCreate() {
        Medication created = repository.create(medication);
        assertNotNull(created);
    }

    @Test
    public void testRead() {
        repository.create(medication);
        Medication read = repository.read("M001");
        assertNotNull(read);
        assertEquals("M001", read.getMedicationId());
    }

    @Test
    public void testUpdate() {
        repository.create(medication);
        Medication updated = new Medication.Builder()
                .copy(medication)
                .setMedicationName("Panado Extra")
                .build();
        Medication result = repository.update(updated);
        assertNotNull(result);
        assertEquals("Panado Extra", result.getMedicationName());
    }

    @Test
    public void testDelete() {
        repository.create(medication);
        assertTrue(repository.delete("M001"));
        assertNull(repository.read("M001"));
    }
}