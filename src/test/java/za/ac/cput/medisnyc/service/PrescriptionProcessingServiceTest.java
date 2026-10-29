package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.OrderPaymentStatus;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionCollectionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.AuditLogJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.NotificationJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.DoctorJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import za.ac.cput.medisnyc.domain.Notification;
import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.domain.Medication;
import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.domain.PrescriptionItem;
import za.ac.cput.medisnyc.dto.PrescriptionQueueItem;
import za.ac.cput.medisnyc.exception.InsufficientStockException;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

class PrescriptionProcessingServiceTest {

    @Test
    void advancesToPreparingAfterCreatingPrescriptionOrder() {
        Prescription existing = prescription(PrescriptionProcessingStatus.RECEIVED);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByIdForUpdate("RX-1")).thenReturn(Optional.of(existing));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        when(orders.getByPrescriptionId("RX-1")).thenReturn(new Order.Builder()
                .setOrderId("ORD-1")
                .setPatientId("PAT-1")
                .setPrescriptionId("RX-1")
                .setDispensingStatus(OrderStatus.PROCESSING)
                .setPaymentStatus(OrderPaymentStatus.PAID)
                .build());
        PrescriptionProcessingService service = service(prescriptions, orders);

        Prescription updated = service.advanceStatus("RX-1");

        assertEquals(PrescriptionProcessingStatus.PREPARING, updated.getProcessingStatus());
        assertEquals("ORD-1", updated.getLinkedOrderId());
        verify(orders).createForPrescription(any(Prescription.class), any());
    }

    @Test
    void doesNotAdvanceOrNotifyWhenStockIsInsufficient() {
        Prescription existing = prescription(PrescriptionProcessingStatus.RECEIVED);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByIdForUpdate("RX-1")).thenReturn(Optional.of(existing));
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        when(orders.createForPrescription(any(Prescription.class), any()))
                .thenThrow(new InsufficientStockException(List.of("Gaviscon")));
        NotificationJpaRepository notificationRepository = mock(NotificationJpaRepository.class);
        NotificationService notifications = new NotificationService(notificationRepository);
        PrescriptionProcessingService service = new PrescriptionProcessingService(
                prescriptions, mock(PrescriptionCollectionJpaRepository.class), notifications,
                new AuditLogService(mock(AuditLogJpaRepository.class)),
                mock(PrescriptionItemJpaRepository.class), orders,
                mock(PatientJpaRepository.class), mock(DoctorJpaRepository.class),
                mock(MedicationJpaRepository.class));

        InsufficientStockException error = assertThrows(InsufficientStockException.class,
                () -> service.advanceStatus("RX-1"));

        assertEquals("Insufficient stock for medications: Gaviscon", error.getMessage());
        assertEquals(PrescriptionProcessingStatus.RECEIVED, existing.getProcessingStatus());
        verify(prescriptions, never()).save(any(Prescription.class));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void completesLinkedOrderWhenPrescriptionIsCollected() {
        Prescription existing = prescription(PrescriptionProcessingStatus.READY_FOR_COLLECTION);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByIdForUpdate("RX-1")).thenReturn(Optional.of(existing));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        when(orders.getByPrescriptionId("RX-1")).thenReturn(new Order.Builder()
                .setOrderId("ORD-1")
                .setPatientId("PAT-1")
                .setPrescriptionId("RX-1")
                .setDispensingStatus(OrderStatus.PROCESSING)
                .setPaymentStatus(OrderPaymentStatus.PAID)
                .build());
        PrescriptionProcessingService service = service(prescriptions, orders);

        Prescription collected = service.advanceStatus("RX-1");

        assertEquals(PrescriptionProcessingStatus.COLLECTED, collected.getProcessingStatus());
        var orderSequence = inOrder(orders);
        orderSequence.verify(orders).updateStatus("ORD-1", OrderStatus.DISPENSED, "SYSTEM");
        orderSequence.verify(orders).updateStatus("ORD-1", OrderStatus.COMPLETED, "SYSTEM");
    }

    @Test
    void cancelsLinkedOrderWhenPrescriptionIsCancelled() {
        Prescription existing = prescription(PrescriptionProcessingStatus.PREPARING);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findById("RX-1")).thenReturn(Optional.of(existing));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        PrescriptionProcessingService service = service(prescriptions, orders);

        Prescription cancelled = service.updateClinicalStatus("RX-1", PrescriptionStatus.CANCELLED);

        assertEquals(PrescriptionStatus.CANCELLED, cancelled.getStatus());
        verify(orders).cancelForPrescription("RX-1", "SYSTEM");
    }

    @Test
    void queueIsOrderedNewestFirstAndExcludesCollectedRows() {
        Prescription older = prescription(PrescriptionProcessingStatus.PENDING);
        ReflectionTestUtils.setField(older, "createdAt", LocalDateTime.now().minusMinutes(5));
        Prescription newer = prescription(PrescriptionProcessingStatus.RECEIVED);
        ReflectionTestUtils.setField(newer, "prescriptionId", "RX-2");
        ReflectionTestUtils.setField(newer, "createdAt", LocalDateTime.now());
        Prescription collected = prescription(PrescriptionProcessingStatus.COLLECTED);
        ReflectionTestUtils.setField(collected, "prescriptionId", "RX-3");
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findAll()).thenReturn(List.of(older, newer, collected));

        List<PrescriptionQueueItem> queue = service(prescriptions,
                mock(PrescriptionOrderService.class)).getQueue();

        assertEquals(List.of("RX-2", "RX-1"),
                queue.stream().map(PrescriptionQueueItem::prescriptionId).toList());
    }

    @Test
    void nullProcessingStatusIsTreatedAsPendingInQueueAndWhenAdvancing() {
        Prescription existing = prescription(PrescriptionProcessingStatus.PENDING);
        ReflectionTestUtils.setField(existing, "processingStatus", null);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findAll()).thenReturn(List.of(existing));
        when(prescriptions.findByIdForUpdate("RX-1")).thenReturn(Optional.of(existing));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PrescriptionProcessingService service = service(prescriptions, mock(PrescriptionOrderService.class));

        assertEquals(PrescriptionProcessingStatus.PENDING,
                service.getQueue().get(0).processingStatus());
        assertEquals(PrescriptionProcessingStatus.RECEIVED,
                service.advanceStatus("RX-1").getProcessingStatus());
    }

    @Test
    void queueIncludesPatientDoctorAndMedicineDetails() {
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findAll()).thenReturn(List.of(prescription(PrescriptionProcessingStatus.PENDING)));
        PrescriptionItem item = new PrescriptionItem.Builder()
                .setPrescriptionItemId("RXI-1")
                .setPrescriptionId("RX-1")
                .setMedicationId("MED-1")
                .setQuantity(3)
                .build();
        PrescriptionItemJpaRepository items = mock(PrescriptionItemJpaRepository.class);
        when(items.findByPrescriptionId("RX-1")).thenReturn(List.of(item));
        Patient patient = new Patient.Builder().setMedicalId("PAT-1")
                .setFirstName("Patient").setLastName("Name").build();
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(patients.findById("PAT-1")).thenReturn(Optional.of(patient));
        Doctor doctor = new Doctor.Builder().setDoctorId("DOC-1")
                .setFirstName("Doctor").setLastName("Name").build();
        DoctorJpaRepository doctors = mock(DoctorJpaRepository.class);
        when(doctors.findById("DOC-1")).thenReturn(Optional.of(doctor));
        Medication medication = new Medication.Builder().setMedicationId("MED-1")
                .setMedicationName("Gaviscon").build();
        MedicationJpaRepository medications = mock(MedicationJpaRepository.class);
        when(medications.findById("MED-1")).thenReturn(Optional.of(medication));
        PrescriptionProcessingService service = new PrescriptionProcessingService(
                prescriptions, mock(PrescriptionCollectionJpaRepository.class),
                new NotificationService(mock(NotificationJpaRepository.class)),
                new AuditLogService(mock(AuditLogJpaRepository.class)), items,
                mock(PrescriptionOrderService.class), patients, doctors, medications);

        PrescriptionQueueItem row = service.getQueue().get(0);

        assertEquals("Patient Name", row.patientName());
        assertEquals("Doctor Name", row.doctorName());
        assertEquals("Gaviscon", row.medicines().get(0).name());
        assertEquals(3, row.medicines().get(0).quantity());
    }

    @Test
    void createsNotificationForEveryProcessingStatusChange() {
        AtomicReference<Prescription> current = new AtomicReference<>(
                prescription(PrescriptionProcessingStatus.PENDING));
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByIdForUpdate("RX-1")).thenAnswer(invocation -> Optional.of(current.get()));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            current.set(saved);
            return saved;
        });

        Order order = new Order.Builder().setOrderId("ORD-1").setPrescriptionId("RX-1")
                .setPatientId("PAT-1").setDispensingStatus(OrderStatus.PROCESSING)
                .setTotalAmount(new java.math.BigDecimal("10.00")).build();
        Order paidOrder = new Order.Builder().setOrderId("ORD-1").setPrescriptionId("RX-1")
                .setPatientId("PAT-1").setDispensingStatus(OrderStatus.PROCESSING)
                .setPaymentStatus(OrderPaymentStatus.PAID).build();
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        when(orders.getByPrescriptionId("RX-1"))
                .thenReturn(order, order, order, order, paidOrder, paidOrder);
        NotificationJpaRepository notificationRepository = mock(NotificationJpaRepository.class);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PrescriptionProcessingService service = new PrescriptionProcessingService(
                prescriptions, mock(PrescriptionCollectionJpaRepository.class),
                new NotificationService(notificationRepository),
                new AuditLogService(mock(AuditLogJpaRepository.class)),
                mock(PrescriptionItemJpaRepository.class), orders,
                mock(PatientJpaRepository.class), mock(DoctorJpaRepository.class),
                mock(MedicationJpaRepository.class));

        for (int i = 0; i < 4; i++) {
            service.advanceStatus("RX-1");
        }

        ArgumentCaptor<Notification> notifications = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(4)).save(notifications.capture());
        assertEquals(List.of(
                        "Prescription RX-1 status changed to Received.",
                        "Prescription RX-1 status changed to Preparing.",
                        "Prescription RX-1 status changed to Ready for collection. Amount due: R10.00.",
                        "Prescription RX-1 status changed to Collected."),
                notifications.getAllValues().stream().map(Notification::getMessage).toList());
        assertEquals(List.of("RX-1", "RX-1", "RX-1", "RX-1"),
                notifications.getAllValues().stream().map(Notification::getPrescriptionId).toList());
    }

    @Test
    void refusesCollectionWhenOrderIsUnpaid() {
        Prescription existing = prescription(PrescriptionProcessingStatus.READY_FOR_COLLECTION);
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByIdForUpdate("RX-1")).thenReturn(Optional.of(existing));
        PrescriptionOrderService orders = mock(PrescriptionOrderService.class);
        when(orders.getByPrescriptionId("RX-1")).thenReturn(new Order.Builder()
                .setOrderId("ORD-1").setPatientId("PAT-1").setTotalAmount(new java.math.BigDecimal("12.50"))
                .build());
        PrescriptionProcessingService service = service(prescriptions, orders);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.advanceStatus("RX-1"));

        assertEquals("Order must be paid before collection. Amount due: R12.50", error.getMessage());
        verify(prescriptions, never()).save(any(Prescription.class));
    }

    private PrescriptionProcessingService service(PrescriptionJpaRepository prescriptions,
                                                   PrescriptionOrderService orders) {
        return new PrescriptionProcessingService(prescriptions, mock(PrescriptionCollectionJpaRepository.class),
                new NotificationService(mock(NotificationJpaRepository.class)),
                new AuditLogService(mock(AuditLogJpaRepository.class)),
                mock(PrescriptionItemJpaRepository.class), orders,
                mock(PatientJpaRepository.class), mock(DoctorJpaRepository.class),
                mock(MedicationJpaRepository.class));
    }

    private Prescription prescription(PrescriptionProcessingStatus processingStatus) {
        return new Prescription.Builder()
                .setPrescriptionId("RX-1")
                .setPatientId("PAT-1")
                .setDoctorId("DOC-1")
                .setDateIssued(LocalDate.now())
                .setExpiryDate(LocalDate.now().plusDays(30))
                .setStatus(PrescriptionStatus.ACTIVE)
                .setProcessingStatus(processingStatus)
                .build();
    }
}
