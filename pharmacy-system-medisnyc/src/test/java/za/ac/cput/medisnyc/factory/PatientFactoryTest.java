package za.ac.cput.medisnyc.factory;




import org.junit.*;
import za.ac.cput.medisnyc.domain.Patient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PatientFactoryTest {

    private static Patient patient1;
    private static Patient patient2;
    private static final LocalDate dob = LocalDate.of(1990, 5, 15);

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= PATIENT FACTORY TESTS =============================");
    }

    @Test
    public void testCreatePatient_Success() {
        System.out.println("Test 1: Create Patient - Success");

        List<String> allergies = Arrays.asList("penicillin", "aspirin");

        patient1 = PatientFactory.createPatient(
                "MED001",
                "John",
                "Doe",
                "john.doe@email.com",
                "0821234567",
                dob,
                allergies
        );

        assertNotNull(patient1);
        assertEquals("MED001", patient1.getMedicalId());
        assertEquals("John", patient1.getFirstName());
        assertEquals("Doe", patient1.getLastName());
        assertEquals(2, patient1.getAllergies().size());
        assertTrue(patient1.hasAllergy("penicillin"));

        System.out.println("✓ Patient created successfully: " + patient1);
    }

    @Test
    public void testCreatePatient_WithoutAllergies() {
        System.out.println("Test 2: Create Patient - Without Allergies");

        patient2 = PatientFactory.createPatient(
                "MED002",
                "Jane",
                "Smith",
                "jane.smith@email.com",
                "0839876543",
                LocalDate.of(1985, 8, 22)
        );

        assertNotNull(patient2);
        assertEquals("MED002", patient2.getMedicalId());
        assertTrue(patient2.getAllergies().isEmpty());

        System.out.println("✓ Patient without allergies created: " + patient2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePatient_NullMedicalId() {
        System.out.println("Test 3: Create Patient - Null Medical ID (Should Fail)");

        PatientFactory.createPatient(
                null,
                "John",
                "Doe",
                "john@email.com",
                "0821234567",
                dob
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePatient_InvalidEmail() {
        System.out.println("Test 4: Create Patient - Invalid Email (Should Fail)");

        PatientFactory.createPatient(
                "MED003",
                "John",
                "Doe",
                "invalid-email",
                "0821234567",
                dob
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePatient_FutureDateOfBirth() {
        System.out.println("Test 5: Create Patient - Future Date of Birth (Should Fail)");

        PatientFactory.createPatient(
                "MED003",
                "John",
                "Doe",
                "john@email.com",
                "0821234567",
                LocalDate.now().plusYears(1)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePatient_EmptyFirstName() {
        System.out.println("Test 6: Create Patient - Empty First Name (Should Fail)");

        PatientFactory.createPatient(
                "MED003",
                "   ",
                "Doe",
                "john@email.com",
                "0821234567",
                dob
        );
    }

    @Test
    public void testPatientEquality() {
        System.out.println("Test 7: Patient Equality");

        if (patient1 == null) {
            patient1 = PatientFactory.createPatient(
                    "MED001",
                    "John",
                    "Doe",
                    "john.doe@email.com",
                    "0821234567",
                    LocalDate.of(1990, 5, 15),
                    null
            );
        }

        Patient patient3 = PatientFactory.createPatient(
                "MED001",
                "Different",
                "Name",
                "different@email.com",
                "0999999999",
                LocalDate.of(2000, 1, 1)
        );

        assertNotNull(patient1);
        assertEquals(patient1, patient3);
        assertEquals(patient1.hashCode(), patient3.hashCode());
        assertNotEquals(patient1, patient2);

        System.out.println("✓ Equality tests passed");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= PATIENT FACTORY TESTS COMPLETED ===================\n");
    }
}