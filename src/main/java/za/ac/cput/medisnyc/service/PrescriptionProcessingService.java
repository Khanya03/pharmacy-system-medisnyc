package za.ac.cput.medisnyc.service;

/* PrescriptionProcessingService.java
   Module 5: Prescription Processing Module.
   Enforces the Pending -> Received -> Preparing -> Ready for Collection -> Collected flow.
   Author: Naledi Ngobeni
*/

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionCollection;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.dto.PrescriptionQueueItem;
import za.ac.cput.medisnyc.exception.ResourceNotFoundException;
import za.ac.cput.medisnyc.repository.jpa.DoctorJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionCollectionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PrescriptionProcessingService {

    private final PrescriptionJpaRepository prescriptionRepository;
    private final PrescriptionCollectionJpaRepository collectionRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final PrescriptionItemJpaRepository prescriptionItemRepository;
    private final PrescriptionOrderService orderService;
    private final PatientJpaRepository patientRepository;
    private final DoctorJpaRepository doctorRepository;
    private final MedicationJpaRepository medicationRepository;

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
                                         NotificationService notificationService,
                                         AuditLogService auditLogService,
                                         PrescriptionItemJpaRepository prescriptionItemRepository,
                                         PrescriptionOrderService orderService,
                                         PatientJpaRepository patientRepository,
                                         DoctorJpaRepository doctorRepository,
                                         MedicationJpaRepository medicationRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.collectionRepository = collectionRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.orderService = orderService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.medicationRepository = medicationRepository;
    }

    public List<PrescriptionQueueItem> getQueue() {
        return prescriptionRepository.findAll().stream()
                .filter(p -> normalizedStatus(p.getProcessingStatus())
                        != PrescriptionProcessingStatus.COLLECTED)
                .sorted(Comparator
                        .comparing(Prescription::getDateIssued,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Prescription::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toQueueItem)
                .toList();
    }

    // Module 5: Prescription Tracking - lets a patient see their own prescriptions
    // and follow each one through Pending -> ... -> Collected.
    public List<Prescription> getByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<Prescription> getByDoctor(String doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    public List<Prescription> getReadyForCollection() {
        return prescriptionRepository.findByProcessingStatus(PrescriptionProcessingStatus.READY_FOR_COLLECTION);
    }

    public Prescription getById(String prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));
    }

    @Transactional
    public Prescription advanceStatus(String prescriptionId) {
        Prescription existing = prescriptionRepository.findByIdForUpdate(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));
        if (existing.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new IllegalStateException("Prescription is " + existing.getStatus().name().toLowerCase());
        }
        if (existing.getExpiryDate() != null && existing.getExpiryDate().isBefore(LocalDate.now())) {
            Prescription expired = copyWithStatus(existing, PrescriptionStatus.EXPIRED);
            Prescription savedExpired = prescriptionRepository.save(expired);
            orderService.cancelForPrescription(prescriptionId, "SYSTEM");
            auditLogService.log("SYSTEM", "EXPIRE_PRESCRIPTION", prescriptionId,
                    "Prescription expired before collection");
            return withLinkedOrder(savedExpired, orderService.getByPrescriptionId(prescriptionId));
        }
        PrescriptionProcessingStatus current = normalizedStatus(existing.getProcessingStatus());
        PrescriptionProcessingStatus next = NEXT_STATUS.get(current);

        if (next == null) {
            throw new IllegalStateException("Prescription is already at its final status: " + current);
        }

        if (next == PrescriptionProcessingStatus.PREPARING) {
            orderService.createForPrescription(existing,
                    prescriptionItemRepository.findByPrescriptionId(prescriptionId));
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
                .setCreatedAt(existing.getCreatedAt())
                .build();

        Prescription saved = prescriptionRepository.save(updated);
        auditLogService.log("SYSTEM", "ADVANCE_PRESCRIPTION", saved.getPrescriptionId(),
                "Processing status changed from " + current + " to " + next);

        if (next == PrescriptionProcessingStatus.COLLECTED) {
            PrescriptionCollection collection = new PrescriptionCollection.Builder()
                    .setCollectionId("COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .setPrescriptionId(saved.getPrescriptionId())
                    .setPatientId(saved.getPatientId())
                    .build();
            collectionRepository.save(collection);
            var order = orderService.getByPrescriptionId(saved.getPrescriptionId());
            if (order == null) {
                throw new IllegalStateException("No order is linked to prescription " + saved.getPrescriptionId());
            }
            orderService.updateStatus(order.getOrderId(), OrderStatus.DISPENSED, "SYSTEM");
            orderService.updateStatus(order.getOrderId(), OrderStatus.COMPLETED, "SYSTEM");
        }

        notificationService.notify(saved.getPatientId(), saved.getPrescriptionId(),
                "Prescription " + saved.getPrescriptionId() + " status changed to "
                        + statusLabel(next) + ".");
        return withLinkedOrder(saved, orderService.getByPrescriptionId(saved.getPrescriptionId()));
    }

    @Transactional
    public Prescription updateClinicalStatus(String prescriptionId, PrescriptionStatus status) {
        if (status == null || status == PrescriptionStatus.ACTIVE) {
            throw new IllegalArgumentException("Prescription status must be CANCELLED or EXPIRED");
        }
        Prescription existing = getById(prescriptionId);
        if (existing.getProcessingStatus() == PrescriptionProcessingStatus.COLLECTED) {
            throw new IllegalStateException("Collected prescriptions cannot be cancelled or expired");
        }
        Prescription updated = copyWithStatus(existing, status);
        Prescription saved = prescriptionRepository.save(updated);
        orderService.cancelForPrescription(prescriptionId, "SYSTEM");
        auditLogService.log("SYSTEM", "UPDATE_PRESCRIPTION_STATUS", prescriptionId,
                "Prescription status changed to " + status);
        return withLinkedOrder(saved, orderService.getByPrescriptionId(prescriptionId));
    }

    private Prescription copyWithStatus(Prescription prescription, PrescriptionStatus status) {
        return new Prescription.Builder()
                .setPrescriptionId(prescription.getPrescriptionId())
                .setPatientId(prescription.getPatientId())
                .setDoctorId(prescription.getDoctorId())
                .setDateIssued(prescription.getDateIssued())
                .setExpiryDate(prescription.getExpiryDate())
                .setInstructions(prescription.getInstructions())
                .setRefillsAllowed(prescription.getRefillsAllowed())
                .setRefillsUsed(prescription.getRefillsUsed())
                .setStatus(status)
                .setProcessingStatus(prescription.getProcessingStatus())
                .setCreatedAt(prescription.getCreatedAt())
                .build();
    }

    private Prescription withLinkedOrder(Prescription prescription, Order order) {
        return new Prescription.Builder()
                .setPrescriptionId(prescription.getPrescriptionId())
                .setPatientId(prescription.getPatientId())
                .setDoctorId(prescription.getDoctorId())
                .setDateIssued(prescription.getDateIssued())
                .setExpiryDate(prescription.getExpiryDate())
                .setInstructions(prescription.getInstructions())
                .setRefillsAllowed(prescription.getRefillsAllowed())
                .setRefillsUsed(prescription.getRefillsUsed())
                .setStatus(prescription.getStatus())
                .setProcessingStatus(prescription.getProcessingStatus())
                .setCreatedAt(prescription.getCreatedAt())
                .setLinkedOrderId(order == null ? null : order.getOrderId())
                .build();
    }

    private PrescriptionQueueItem toQueueItem(Prescription prescription) {
        Patient patient = prescription.getPatientId() == null
                ? null : patientRepository.findById(prescription.getPatientId()).orElse(null);
        Doctor doctor = prescription.getDoctorId() == null
                ? null : doctorRepository.findById(prescription.getDoctorId()).orElse(null);
        List<PrescriptionQueueItem.Medicine> medicines = prescriptionItemRepository
                .findByPrescriptionId(prescription.getPrescriptionId()).stream()
                .map(item -> {
                    String medicationId = item.getMedicationId();
                    String medicationName = medicationId == null || medicationId.isBlank()
                            ? "Unknown medication"
                            : medicationRepository.findById(medicationId)
                                    .map(Medication::getMedicationName)
                                    .orElse(medicationId);
                    return new PrescriptionQueueItem.Medicine(
                            medicationId, medicationName, item.getQuantity());
                })
                .toList();
        Order order = orderService.getByPrescriptionId(prescription.getPrescriptionId());

        return new PrescriptionQueueItem(
                prescription.getPrescriptionId(),
                prescription.getPatientId(),
                fullName(patient == null ? null : patient.getFirstName(),
                        patient == null ? null : patient.getLastName(), "Unknown patient"),
                prescription.getDoctorId(),
                fullName(doctor == null ? null : doctor.getFirstName(),
                        doctor == null ? null : doctor.getLastName(), "Unknown doctor"),
                prescription.getDateIssued(),
                prescription.getCreatedAt(),
                prescription.getStatus(),
                normalizedStatus(prescription.getProcessingStatus()),
                order == null ? null : order.getOrderId(),
                medicines);
    }

    private PrescriptionProcessingStatus normalizedStatus(PrescriptionProcessingStatus status) {
        return status == null ? PrescriptionProcessingStatus.PENDING : status;
    }

    private String statusLabel(PrescriptionProcessingStatus status) {
        return switch (status) {
            case RECEIVED -> "Received";
            case PREPARING -> "Preparing";
            case READY_FOR_COLLECTION -> "Ready for collection";
            case COLLECTED -> "Collected";
            case PENDING -> "Pending";
        };
    }

    private String fullName(String firstName, String lastName, String fallback) {
        String name = ((firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName)).trim();
        return name.isEmpty() ? fallback : name;
    }
}