package za.ac.cput.medisnyc.repository;

/* PrescriptionRepository.java
   Prescription factory class
   Author: Naledi Ngobeni (230742912)
   Date: 25 March 2026
*/
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;

import java.util.List;

public interface PrescriptionRepository extends IRepository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByStatus(PrescriptionStatus status);
    List<Prescription> findActivePrescriptions(String patientId);
}