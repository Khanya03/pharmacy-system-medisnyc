package za.ac.cput.medisnyc.service;

/* ConsultationService.java
   Module 3: Doctor Consultation & Prescription Module.
*/

import za.ac.cput.medisnyc.domain.MedicalRecord;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionItem;
import za.ac.cput.medisnyc.repository.jpa.MedicalRecordJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultationService {

    private final MedicalRecordJpaRepository medicalRecordRepository;
    private final PrescriptionJpaRepository prescriptionRepository;
    private final PrescriptionItemJpaRepository prescriptionItemRepository;

    @Autowired
    public ConsultationService(MedicalRecordJpaRepository medicalRecordRepository,
                               PrescriptionJpaRepository prescriptionRepository,
                               PrescriptionItemJpaRepository prescriptionItemRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
    }

    // Today's appointments handled by AppointmentService.getByDoctor + date filter in controller.

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        MedicalRecord toSave = record.getRecordId() == null || record.getRecordId().isBlank()
                ? new MedicalRecord.Builder().copy(record)
                  .setRecordId("MR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                  .build()
                : record;
        return medicalRecordRepository.save(toSave);
    }

    public List<MedicalRecord> getRecordsByPatient(String patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }

    public List<MedicalRecord> getRecordsByDoctor(String doctorId) {
        return medicalRecordRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public Prescription createPrescription(Prescription prescription, List<PrescriptionItem> items) {
        Prescription toSave = prescription.getPrescriptionId() == null || prescription.getPrescriptionId().isBlank()
                ? new Prescription.Builder()
                  .setPrescriptionId("RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                  .setPatientId(prescription.getPatientId())
                  .setDoctorId(prescription.getDoctorId())
                  .setDateIssued(prescription.getDateIssued())
                  .setExpiryDate(prescription.getExpiryDate())
                  .setInstructions(prescription.getInstructions())
                  .setRefillsAllowed(prescription.getRefillsAllowed())
                  .setRefillsUsed(prescription.getRefillsUsed())
                  .setStatus(prescription.getStatus())
                  .setProcessingStatus(prescription.getProcessingStatus())
                  .build()
                : prescription;

        Prescription saved = prescriptionRepository.save(toSave);

        if (items != null) {
            for (PrescriptionItem item : items) {
                PrescriptionItem itemToSave = new PrescriptionItem.Builder()
                        .setPrescriptionItemId("RXI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .setPrescriptionId(saved.getPrescriptionId())
                        .setMedicationId(item.getMedicationId())
                        .setQuantity(item.getQuantity())
                        .setDosage(item.getDosage())
                        .setFrequency(item.getFrequency())
                        .setDurationDays(item.getDurationDays())
                        .build();
                prescriptionItemRepository.save(itemToSave);
            }
        }

        return saved;
    }

    public List<PrescriptionItem> getItemsForPrescription(String prescriptionId) {
        return prescriptionItemRepository.findByPrescriptionId(prescriptionId);
    }
}
