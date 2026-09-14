/* FactoryTest.java
   Utility FactoryTest class
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

package za.ac.cput.medisnyc.factory;

import za.ac.cput.medisnyc.domain.Doctor;
import org.junit.Test;
import static org.junit.Assert.*;

public class DoctorFactoryTest {

    @Test
    public void testCreateDoctor_Success() {
        System.out.println("=== Testing DoctorFactory ===");

        Doctor doctor = DoctorFactory.createDoctor(
                "DOC001",
                "Khanya",
                "Mpahla",
                "Cardiology",
                "0841234567",
                "khanyampahla@hospital.com"
        );

        assertNotNull(doctor);
        assertEquals("DOC001", doctor.getDoctorId());
        assertEquals("Khanya", doctor.getFirstName());
        assertEquals("Mpahla", doctor.getLastName());
        assertEquals("Cardiology", doctor.getSpecialization());
        assertEquals("0841234567", doctor.getPhoneNumber());
        assertEquals("khanyampahla@hospital.com", doctor.getEmail());

        System.out.println("✓ Doctor created successfully: " + doctor.getDoctorId());
        System.out.println("  Name: " + doctor.getFirstName() + " " + doctor.getLastName());
        System.out.println("  Specialization: " + doctor.getSpecialization());
    }

    @Test
    public void testCreateDoctor_InvalidNullFields() {
        System.out.println("\n=== Testing Invalid Doctor Creation (Null Fields) ===");

        // Test null doctorId
        Doctor nullDoctorId = DoctorFactory.createDoctor(
                null, "Khanya", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Doctor with null ID should return null", nullDoctorId);
        System.out.println("✓ Null doctorId rejected");

        // Test null firstName
        Doctor nullFirstName = DoctorFactory.createDoctor(
                "DOC001", null, "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Doctor with null first name should return null", nullFirstName);
        System.out.println("✓ Null firstName rejected");

        // Test null lastName
        Doctor nullLastName = DoctorFactory.createDoctor(
                "DOC001", "Khanya", null, "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Doctor with null last name should return null", nullLastName);
        System.out.println("✓ Null lastName rejected");

        // Test null specialization
        Doctor nullSpecialization = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", null, "0841234567", "khanya@hospital.com"
        );
        assertNull("Doctor with null specialization should return null", nullSpecialization);
        System.out.println("✓ Null specialization rejected");

        // Test null phoneNumber
        Doctor nullPhoneNumber = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", null, "khanya@hospital.com"
        );
        assertNull("Doctor with null phone number should return null", nullPhoneNumber);
        System.out.println("✓ Null phoneNumber rejected");

        // Test null email
        Doctor nullEmail = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", null
        );
        assertNull("Doctor with null email should return null", nullEmail);
        System.out.println("✓ Null email rejected");
    }

    @Test
    public void testCreateDoctor_InvalidEmail() {
        System.out.println("\n=== Testing Invalid Email Formats ===");

        // Test invalid email (no @ symbol)
        Doctor invalidEmail1 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", "not-an-email"
        );
        assertNull("Invalid email (no @) should be rejected", invalidEmail1);
        System.out.println("✓ Invalid email (no @) rejected");

        // Test invalid email (no domain)
        Doctor invalidEmail2 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", "test@"
        );
        assertNull("Invalid email (no domain) should be rejected", invalidEmail2);
        System.out.println("✓ Invalid email (no domain) rejected");

        // Test invalid email (no local part)
        Doctor invalidEmail3 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", "@hospital.com"
        );
        assertNull("Invalid email (no local part) should be rejected", invalidEmail3);
        System.out.println("✓ Invalid email (no local part) rejected");

        // Test empty email
        Doctor emptyEmail = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", ""
        );
        assertNull("Empty email should be rejected", emptyEmail);
        System.out.println("✓ Empty email rejected");
    }

    @Test
    public void testCreateDoctor_InvalidPhone() {
        System.out.println("\n=== Testing Invalid Phone Formats ===");

        // Test phone too short
        Doctor invalidPhone1 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "123", "khanya@hospital.com"
        );
        assertNull("Phone number too short should be rejected", invalidPhone1);
        System.out.println("✓ Phone number too short (3 digits) rejected");

        // Test phone with letters
        Doctor invalidPhone2 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "ABCDEFGHIJ", "khanya@hospital.com"
        );
        assertNull("Phone number with letters should be rejected", invalidPhone2);
        System.out.println("✓ Phone number with letters rejected");

        // Test phone with special characters
        Doctor invalidPhone3 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "084-123-4567", "khanya@hospital.com"
        );
        assertNull("Phone number with special characters should be rejected", invalidPhone3);
        System.out.println("✓ Phone number with special characters rejected");

        // Test empty phone
        Doctor emptyPhone = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "", "khanya@hospital.com"
        );
        assertNull("Empty phone number should be rejected", emptyPhone);
        System.out.println("✓ Empty phone number rejected");

        // Test phone too long
        Doctor invalidPhone4 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "084123456789", "khanya@hospital.com"
        );
        assertNull("Phone number too long should be rejected", invalidPhone4);
        System.out.println("✓ Phone number too long (12 digits) rejected");
    }

    @Test
    public void testCreateDoctor_ValidPhoneFormats() {
        System.out.println("\n=== Testing Valid Phone Formats ===");

        // Test standard 10-digit phone
        Doctor validPhone1 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNotNull("Valid 10-digit phone should be accepted", validPhone1);
        assertEquals("0841234567", validPhone1.getPhoneNumber());
        System.out.println("✓ Valid 10-digit phone accepted: " + validPhone1.getPhoneNumber());

        // Test another valid phone number
        Doctor validPhone2 = DoctorFactory.createDoctor(
                "DOC002", "John", "Smith", "Pediatrics", "0719876543", "john@hospital.com"
        );
        assertNotNull("Another valid 10-digit phone should be accepted", validPhone2);
        assertEquals("0719876543", validPhone2.getPhoneNumber());
        System.out.println("✓ Another valid phone accepted: " + validPhone2.getPhoneNumber());
    }

    @Test
    public void testCreateDoctor_ValidEmailFormats() {
        System.out.println("\n=== Testing Valid Email Formats ===");

        // Test standard email
        Doctor validEmail1 = DoctorFactory.createDoctor(
                "DOC001", "Khanya", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNotNull("Valid email should be accepted", validEmail1);
        assertEquals("khanya@hospital.com", validEmail1.getEmail());
        System.out.println("✓ Valid email accepted: " + validEmail1.getEmail());

        // Test email with numbers
        Doctor validEmail2 = DoctorFactory.createDoctor(
                "DOC002", "John", "Smith", "Pediatrics", "0719876543", "john123@medical.co.za"
        );
        assertNotNull("Email with numbers should be accepted", validEmail2);
        assertEquals("john123@medical.co.za", validEmail2.getEmail());
        System.out.println("✓ Email with numbers accepted: " + validEmail2.getEmail());

        // Test email with dot
        Doctor validEmail3 = DoctorFactory.createDoctor(
                "DOC003", "Sarah", "Johnson", "Neurology", "0821234567", "sarah.johnson@hospital.org"
        );
        assertNotNull("Email with dot should be accepted", validEmail3);
        assertEquals("sarah.johnson@hospital.org", validEmail3.getEmail());
        System.out.println("✓ Email with dot accepted: " + validEmail3.getEmail());
    }

    @Test
    public void testCreateDoctor_EmptyStringFields() {
        System.out.println("\n=== Testing Empty String Fields ===");

        // Test empty doctorId
        Doctor emptyDoctorId = DoctorFactory.createDoctor(
                "", "Khanya", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Empty doctorId should be rejected", emptyDoctorId);
        System.out.println("✓ Empty doctorId rejected");

        // Test empty firstName
        Doctor emptyFirstName = DoctorFactory.createDoctor(
                "DOC001", "", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Empty firstName should be rejected", emptyFirstName);
        System.out.println("✓ Empty firstName rejected");

        // Test whitespace-only firstName
        Doctor whitespaceFirstName = DoctorFactory.createDoctor(
                "DOC001", "   ", "Mpahla", "Cardiology", "0841234567", "khanya@hospital.com"
        );
        assertNull("Whitespace-only firstName should be rejected", whitespaceFirstName);
        System.out.println("✓ Whitespace-only firstName rejected");
    }
}