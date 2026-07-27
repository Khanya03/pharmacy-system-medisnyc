package za.ac.cput.medisnyc.repository;

/* PrescriptionRepositoryTest.java
   Prescription Repository Test class
   Author: Naledi Ngobeni (230742912)
   Date: 25 July 2026
*/

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.factory.PrescriptionFactory;
import za.ac.cput.medisnyc.repository.impl.PrescriptionRepositoryImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class PrescriptionRepositoryTest {

    private static PrescriptionRepository repository;
    private static Prescription pres1;
    private static Prescription pres2;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= PRESCRIPTION REPOSITORY TESTS ====================");
        repository = PrescriptionRepositoryImpl.getRepository();
    }

    @Before
    public void setUp() {
        // FIXED: Changed MED001 to PAT001, removed 8th parameter
        pres1 = PrescriptionFactory.createPrescription(
                "PRES001", "PAT001", "LIC001",
                LocalDate.now(), null, "Take daily", 3);

        pres2 = PrescriptionFactory.createPrescription(
                "PRES002", "PAT002", "LIC002",
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                "Take twice daily", 0);

        for (Prescription p : repository.getAll()) {
            repository.delete(p.getPrescriptionId());
        }
    }

    @Test
    public void testCreate() {
        System.out.println("Test 1: Create Prescription");

        Prescription created = repository.create(pres1);
        assertNotNull(created);
        assertEquals("PRES001", created.getPrescriptionId());

        System.out.println("✓ Created: " + created);
    }

    @Test
    public void testRead() {
        System.out.println("Test 2: Read Prescription");

        repository.create(pres1);
        Prescription read = repository.read("PRES001");

        assertNotNull(read);
        assertEquals("PAT001", read.getPatientId());

        System.out.println(" Read: " + read);
    }

    @Test
    public void testFindByPatientId() {
        System.out.println("Test 3: Find By Patient ID");

        repository.create(pres1);
        repository.create(pres2);

        List<Prescription> results = repository.findByPatientId("PAT001");
        assertEquals(1, results.size());

        System.out.println(" Found " + results.size() + " prescription(s) for patient");
    }

    @Test
    public void testFindByStatus() {
        System.out.println("Test 4: Find By Status");

        repository.create(pres1);
        repository.create(pres2);

        List<Prescription> active = repository.findByStatus(PrescriptionStatus.ACTIVE);
        assertEquals(2, active.size());

        System.out.println(" Found " + active.size() + " active prescription(s)");
    }

    @Test
    public void testFindActivePrescriptions() {
        System.out.println("Test 5: Find Active Prescriptions");

        repository.create(pres1);
        repository.create(pres2);

        List<Prescription> active = repository.findActivePrescriptions("PAT001");
        assertEquals(1, active.size());

        System.out.println(" Found " + active.size() + " fillable prescription(s)");
    }

    @Test
    public void testUpdate() {
        System.out.println("Test 6: Update Prescription");

        repository.create(pres1);

        Prescription updated = PrescriptionFactory.createPrescription(
                "PRES001", "PAT001", "LIC001",
                LocalDate.now(), LocalDate.now().plusMonths(3),
                "Updated instructions", 5);

        Prescription result = repository.update(updated);
        assertNotNull(result);
        assertEquals("Updated instructions", result.getInstructions());
        assertEquals(5, result.getRefillsAllowed());

        System.out.println("✓ Updated: " + result);
    }

    @Test
    public void testDelete() {
        System.out.println("Test 7: Delete Prescription");

        repository.create(pres1);
        boolean deleted = repository.delete("PRES001");

        assertTrue(deleted);
        assertNull(repository.read("PRES001"));

        System.out.println(" Deleted successfully");
    }

    @Test
    public void testGetAll() {
        System.out.println("Test 8: Get All Prescriptions");

        repository.create(pres1);
        repository.create(pres2);

        List<Prescription> all = repository.getAll();
        assertEquals(2, all.size());

        System.out.println(" Retrieved " + all.size() + " prescriptions");
    }

    @After
    public void cleanUp() {
        for (Prescription p : repository.getAll()) {
            repository.delete(p.getPrescriptionId());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= PRESCRIPTION REPOSITORY TESTS COMPLETED ==========\n");
    }
}