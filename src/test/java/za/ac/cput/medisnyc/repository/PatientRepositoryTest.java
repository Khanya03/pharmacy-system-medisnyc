package za.ac.cput.medisnyc.repository;


/* PatientRepositoryTest.java
   Patient Repository Test class
   Author: Siphesihle Mposelwa
   Student Number:222330325
   Date:19 March 2026
*/

import org.junit.*;
import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.factory.PatientFactory;
import za.ac.cput.medisnyc.repository.impl.PatientRepositoryImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PatientRepositoryTest {

    private static PatientRepository repository;
    private static Patient patient1;
    private static Patient patient2;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= PATIENT REPOSITORY TESTS ==========================");
        repository = PatientRepositoryImpl.getRepository();
    }

    @Before
    public void setUp() {
        patient1 = PatientFactory.createPatient(
                "MED001",
                "John",
                "Doe",
                "john.doe@email.com",
                "0821234567",
                LocalDate.of(1990, 5, 15),
                Arrays.asList("penicillin")
        );

        patient2 = PatientFactory.createPatient(
                "MED002",
                "Jane",
                "Doe",
                "jane.doe@email.com",
                "0839876543",
                LocalDate.of(1985, 8, 22)
        );


        for (Patient p : repository.getAll()) {
            repository.delete(p.getMedicalId());
        }
    }

    @Test
    public void testCreate() {
        System.out.println("Test 1: Create Patient");

        Patient created = repository.create(patient1);
        assertNotNull(created);
        assertEquals(patient1.getMedicalId(), created.getMedicalId());

        System.out.println("✓ Created: " + created);
    }

    @Test
    public void testRead() {
        System.out.println("Test 2: Read Patient");

        repository.create(patient1);
        Patient read = repository.read("MED001");

        assertNotNull(read);
        assertEquals("John", read.getFirstName());

        System.out.println("✓ Read: " + read);
    }

    @Test
    public void testRead_NotFound() {
        System.out.println("Test 3: Read Patient - Not Found");

        Patient read = repository.read("NONEXISTENT");
        assertNull(read);

        System.out.println("✓ Correctly returned null");
    }

    @Test
    public void testUpdate() {
        System.out.println("Test 4: Update Patient");

        repository.create(patient1);

        Patient updated = PatientFactory.createPatient(
                "MED001",
                "Johnny",
                "Doe",
                "johnny.doe@email.com",
                "0821234567",
                LocalDate.of(1990, 5, 15)
        );

        Patient result = repository.update(updated);
        assertNotNull(result);
        assertEquals("Johnny", result.getFirstName());
        assertEquals("johnny.doe@email.com", result.getEmail());

        System.out.println("✓ Updated: " + result);
    }

    @Test
    public void testDelete() {
        System.out.println("Test 5: Delete Patient");

        repository.create(patient1);
        boolean deleted = repository.delete("MED001");

        assertTrue(deleted);
        assertNull(repository.read("MED001"));

        System.out.println("✓ Deleted successfully");
    }

    @Test
    public void testGetAll() {
        System.out.println("Test 6: Get All Patients");

        repository.create(patient1);
        repository.create(patient2);

        List<Patient> all = repository.getAll();
        assertEquals(2, all.size());

        System.out.println("✓ Retrieved " + all.size() + " patients");
    }

    @Test
    public void testFindByEmail() {
        System.out.println("Test 7: Find By Email");

        repository.create(patient1);
        Patient found = repository.findByEmail("john.doe@email.com");

        assertNotNull(found);
        assertEquals("MED001", found.getMedicalId());

        System.out.println("✓ Found by email: " + found);
    }

    @Test
    public void testFindByLastName() {
        System.out.println("Test 8: Find By Last Name");

        repository.create(patient1);
        repository.create(patient2);

        List<Patient> doePatients = repository.findByLastName("Doe");
        assertEquals(2, doePatients.size());

        System.out.println("✓ Found " + doePatients.size() + " patients with last name 'Doe'");
    }

    @Test
    public void testFindPatientsWithAllergies() {
        System.out.println("Test 9: Find Patients With Allergies");

        repository.create(patient1);
        repository.create(patient2);

        List<Patient> allergic = repository.findPatientsWithAllergies("penicillin");
        assertEquals(1, allergic.size());
        assertEquals("MED001", allergic.get(0).getMedicalId());

        System.out.println("✓ Found " + allergic.size() + " patients with penicillin allergy");
    }

    @Test
    public void testExistsByMedicalId() {
        System.out.println("Test 10: Exists By Medical ID");

        repository.create(patient1);

        assertTrue(repository.existsByMedicalId("MED001"));
        assertFalse(repository.existsByMedicalId("NONEXISTENT"));

        System.out.println("✓ Exists check passed");
    }

    @After
    public void cleanUp() {
        for (Patient p : repository.getAll()) {
            repository.delete(p.getMedicalId());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= PATIENT REPOSITORY TESTS COMPLETED ================\n");
    }
}