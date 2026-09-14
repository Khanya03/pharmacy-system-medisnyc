package za.ac.cput.medisnyc.repository;

/* DoctorRepositoryTest.java
   Doctor repository Test class
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.factory.DoctorFactory;
import za.ac.cput.medisnyc.repository.impl.DoctorRepositoryImpl;

import java.util.List;

public class DoctorRepositoryTest {

    private static DoctorRepositoryImpl repository;
    private static Doctor doctor1;
    private static Doctor doctor2;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= DOCTOR REPOSITORY TESTS ===========================");
        repository = DoctorRepositoryImpl.getRepository();
    }

    @Before
    public void setUp() {
        repository.clear();

        doctor1 = DoctorFactory.createDoctor(
                "DOC001",
                "Khanya",
                "Mpahla",
                "Cardiology",
                "0841234567",
                "khanya@hospital.com"
        );

        doctor2 = DoctorFactory.createDoctor(
                "DOC002",
                "John",
                "Smith",
                "Pediatrics",
                "0841234568",
                "john@hospital.com"
        );
    }

    @Test
    public void testCreate() {
        System.out.println("Test 1: Create Doctor");

        Doctor created = repository.create(doctor1);

        assertNotNull(created);
        assertEquals("DOC001", created.getDoctorId());

        System.out.println("✓ Created: " + created);
    }

    @Test
    public void testRead() {
        System.out.println("Test 2: Read Doctor");

        repository.create(doctor1);

        Doctor read = repository.read("DOC001");

        assertNotNull(read);
        assertEquals("Khanya", read.getFirstName());

        System.out.println("✓ Read: " + read);
    }

    @Test
    public void testReadNotFound() {
        System.out.println("Test 2b: Read Doctor Not Found");

        Doctor read = repository.read("NONEXISTENT");

        assertNull(read);

        System.out.println("✓ Correctly returned null for non-existent doctor");
    }

    @Test
    public void testFindBySpecialization() {
        System.out.println("Test 3: Find By Specialization");

        repository.create(doctor1);
        repository.create(doctor2);

        List<Doctor> results = repository.findBySpecialization("Cardiology");

        assertEquals(1, results.size());
        assertEquals("DOC001", results.get(0).getDoctorId());

        System.out.println("✓ Found " + results.size() + " doctor(s) for specialization");
    }

    @Test
    public void testFindByLastName() {
        System.out.println("Test 4: Find By Last Name");

        repository.create(doctor1);
        repository.create(doctor2);

        List<Doctor> results = repository.findByLastName("Mpahla");

        assertEquals(1, results.size());
        assertEquals("DOC001", results.get(0).getDoctorId());

        System.out.println("✓ Found " + results.size() + " doctor(s) with last name");
    }

    @Test
    public void testFindByFirstName() {
        System.out.println("Test 5: Find By First Name");

        repository.create(doctor1);
        repository.create(doctor2);

        List<Doctor> results = repository.findByFirstName("Khanya");

        assertEquals(1, results.size());
        assertEquals("DOC001", results.get(0).getDoctorId());

        System.out.println("✓ Found " + results.size() + " doctor(s) with first name");
    }

    @Test
    public void testUpdate() {
        System.out.println("Test 6: Update Doctor");

        repository.create(doctor1);

        Doctor updated = Doctor.Builder.copy(doctor1)
                .setSpecialization("Neurology")
                .build();

        Doctor result = repository.update(updated);

        assertNotNull(result);
        assertEquals("Neurology", result.getSpecialization());

        System.out.println("✓ Updated: " + result);
    }

    @Test
    public void testDelete() {
        System.out.println("Test 7: Delete Doctor");

        repository.create(doctor1);

        boolean deleted = repository.delete("DOC001");

        assertTrue(deleted);

        Doctor read = repository.read("DOC001");
        assertNull(read);

        System.out.println("✓ Deleted successfully");
    }

    @Test
    public void testGetAll() {
        System.out.println("Test 8: Get All Doctors");

        repository.create(doctor1);
        repository.create(doctor2);

        List<Doctor> all = repository.getAll();

        assertEquals(2, all.size());

        System.out.println("✓ Found " + all.size() + " total doctor(s)");
    }
}