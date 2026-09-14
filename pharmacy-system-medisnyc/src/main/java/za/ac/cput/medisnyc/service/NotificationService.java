package za.ac.cput.medisnyc.service;

/* NotificationService.java
   Module 5: Prescription Processing Module - Notify Patient API.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.Notification;
import za.ac.cput.medisnyc.repository.jpa.NotificationJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationJpaRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationJpaRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification notify(String patientId, String prescriptionId, String message) {
        Notification notification = new Notification.Builder()
                .setPatientId(patientId)
                .setPrescriptionId(prescriptionId)
                .setMessage(message)
                .setRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getForPatient(String patientId) {
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<Notification> getUnreadForPatient(String patientId) {
        return notificationRepository.findByPatientIdAndIsReadFalse(patientId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification existing = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        Notification updated = new Notification.Builder()
                .setNotificationId(existing.getNotificationId())
                .setPatientId(existing.getPatientId())
                .setPrescriptionId(existing.getPrescriptionId())
                .setMessage(existing.getMessage())
                .setRead(true)
                .setCreatedAt(existing.getCreatedAt())
                .build();
        return notificationRepository.save(updated);
    }
}
