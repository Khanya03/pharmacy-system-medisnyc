package za.ac.cput.medisnyc.service;

/* PrescriptionProcessingService.java
   Module 5: Prescription Processing Module.
   Enforces the Pending -> Received -> Preparing -> Ready for Collection -> Collected flow.
   Author: Naledi Ngobeni
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionCollection;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionCollectionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PrescriptionProcessingService {

    private final PrescriptionJpaRepository prescriptionRepository;
    private final PrescriptionCollectionJpaRepository collectionRepository;
    private final NotificationService notificationService;

    // Defines which status can move to which next status.
    private static final Map<PrescriptionProcessingStatus, PrescriptionProcessingStatus> NEXT_STATUS = Map.of(
            PrescriptionProcessingStatus.PENDING, PrescriptionProcessingStatus.RECEIVED,
            PrescriptionProcessingStatus.RECEIVED, PrescriptionProcessingStatus.PREPARING,
            PrescriptionProcessingStatus.PREPARING, PrescriptionProcessingStatus.READY_FOR_COLLECTION,
            PrescriptionProcessingStatus.READY_FOR_COLLECTION, PrescriptionProcessingStatus.COLLECTED
    );

    @Autowired
    public PrescriptionProcessingService(PrescriptionJpaRepository prescriptionRepository,
                                         PrescriptionCollectionJpaRepository collectionRepository,
                                         NotificationService notificationService) {
        this.prescriptionRepository = prescriptionRepository;
        this.collectionRepository = collectionRepository;
        this.notificationService = notificationService;
    }

    public List<Prescription> getQueue() {
        // Everything not yet collected - the pharmacist's prescription queue.
        return prescriptionRepository.findAll().stream()
                .filter(p -> p.getProcessingStatus() != PrescriptionProcessingStatus.COLLECTED)
                .toList();
    }

    // Module 5: Prescription Tracking - lets a patient see their own prescriptions
    // and follow each one through Pending -> ... -> Collected.
    public List<Prescription> getByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<Prescription> getReadyForCollection() {
        return prescriptionRepository.findByProcessingStatus(PrescriptionProcessingStatus.READY_FOR_COLLECTION);
    }

    public Prescription getById(String prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found: " + prescriptionId));
    }

    @Transactional
    public Prescription advanceStatus(String prescriptionId) {
        Prescription existing = getById(prescriptionId);
        PrescriptionProcessingStatus current = existing.getProcessingStatus();
        PrescriptionProcessingStatus next = NEXT_STATUS.get(current);

        if (next == null) {
            throw new IllegalStateException("Prescription is already at its final status: " + current);
        }

        Prescription updated = new Prescription.Builder()
                .setPrescriptionId(existing.getPrescriptionId())
                .setPatientId(existing.getPatientId())
                .setDoctorId(existing.getDoctorId())
                .setDateIssued(existing.getDateIssued())
                .setExpiryDate(existing.getExpiryDate())
                .setInstructions(existing.getInstructions())
                .setRefillsAllowed(existing.getRefillsAllowed())
                .setRefillsUsed(existing.getRefillsUsed())
                .setStatus(existing.getStatus())
                .setProcessingStatus(next)
                .build();

        Prescription saved = prescriptionRepository.save(updated);

        if (next == PrescriptionProcessingStatus.READY_FOR_COLLECTION) {
            notificationService.notify(saved.getPatientId(), saved.getPrescriptionId(),
                    "Your prescription " + saved.getPrescriptionId() + " is ready for collection.");
        }

        if (next == PrescriptionProcessingStatus.COLLECTED) {
            PrescriptionCollection collection = new PrescriptionCollection.Builder()
                    .setCollectionId("COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .setPrescriptionId(saved.getPrescriptionId())
                    .setPatientId(saved.getPatientId())
                    .build();
            collectionRepository.save(collection);
        }

        return saved;
    }
}