package za.ac.cput.medisnyc.factory;

/* PrescriptionFactory.java
   Prescription factory class
   Author: Naledi Ngobeni (230742912)
   Date: 20 july 2026
*/

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class PrescriptionFactoryTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("============================= PRESCRIPTION FACTORY TESTS =======================");
    }

    @Test
    public void testCreatePrescription_Success() {
        System.out.println("Test 1: Create Prescription - Success");


        Prescription prescription1 = PrescriptionFactory.createPrescription(
                "PRES001",
                "PAT001",
                "LIC001",
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Take one tablet daily after meals",
                3
        );

        assertNotNull(prescription1);
        assertEquals("PRES001", prescription1.getPrescriptionId());
        assertEquals("PAT001", prescription1.getPatientId());
        assertEquals("LIC001", prescription1.getDoctorId());
        assertEquals(PrescriptionStatus.ACTIVE, prescription1.getStatus());
        assertTrue(prescription1.canBeFilled());

        System.out.println("✓ Prescription created: " + prescription1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePrescription_NullPrescriptionId() {
        System.out.println("Test 2: Create Prescription - Null ID");


        PrescriptionFactory.createPrescription(
                null,
                "PAT001",
                "LIC001",
                LocalDate.now(),
                null,
                "Take twice daily",
                2
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePrescription_NegativeRefills() {
        System.out.println("Test 3: Create Prescription - Negative Refills");


        PrescriptionFactory.createPrescription(
                "PRES002",
                "PAT001",
                "LIC001",
                LocalDate.now(),
                null,
                "Take as needed",
                -1
        );
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("============================= PRESCRIPTION FACTORY TESTS COMPLETED ============\n");
    }
}