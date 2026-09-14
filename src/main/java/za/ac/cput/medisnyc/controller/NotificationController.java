package za.ac.cput.medisnyc.controller;

/* NotificationController.java
   Module 5: Prescription Processing Module - Notify Patient API.

*/

import za.ac.cput.medisnyc.domain.Notification;
import za.ac.cput.medisnyc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Notification>> getForPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(notificationService.getForPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/unread")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable String patientId) {
        return ResponseEntity.ok(notificationService.getUnreadForPatient(patientId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
}
