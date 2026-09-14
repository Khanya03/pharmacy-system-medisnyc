package za.ac.cput.medisnyc.factory;

/* PrescriptionFactory.java
   Prescription factory class
   Author: Naledi Ngobeni (230742912)
   Date: 20 july 2026
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.util.Helper;

import java.time.LocalDate;

public class PrescriptionFactory {

    public static Prescription createPrescription(String prescriptionId, String patientId,
                                                  String doctorId, LocalDate dateIssued,
                                                  LocalDate expiryDate, String instructions,
                                                  int refillsAllowed) {

        if (Helper.isNullOrEmpty(prescriptionId)) {
            throw new IllegalArgumentException("Prescription ID cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(patientId)) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (Helper.isNullOrEmpty(doctorId)) {
            throw new IllegalArgumentException("Doctor ID cannot be null or empty");
        }
        if (refillsAllowed < 0) {
            throw new IllegalArgumentException("Refills allowed cannot be negative");
        }

        return new Prescription.Builder()
                .setPrescriptionId(prescriptionId.trim().toUpperCase())
                .setPatientId(patientId.trim().toUpperCase())
                .setDoctorId(doctorId.trim().toUpperCase())
                .setDateIssued(dateIssued)
                .setExpiryDate(expiryDate)
                .setInstructions(instructions)
                .setRefillsAllowed(refillsAllowed)
                .setRefillsUsed(0)
                .setStatus(PrescriptionStatus.ACTIVE)
                .build();
    }
}