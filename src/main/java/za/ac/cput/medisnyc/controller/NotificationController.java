package za.ac.cput.medisnyc.controller;

/* NotificationController.java
   Module 5: Prescription Processing Module - Notify Patient API.

*/

import za.ac.cput.medisnyc.domain.Notification;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getForPatient(requirePatientId(user)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Notification>> getForPatient(@PathVariable String patientId,
                                                             @AuthenticationPrincipal User user) {
        requireOwnPatientId(user, patientId);
        return ResponseEntity.ok(notificationService.getForPatient(patientId));
    }

    @GetMapping("/patient/{patientId}/unread")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable String patientId,
                                                         @AuthenticationPrincipal User user) {
        requireOwnPatientId(user, patientId);
        return ResponseEntity.ok(notificationService.getUnreadForPatient(patientId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId,
                                                   @AuthenticationPrincipal User user) {
        String patientId = requirePatientId(user);
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, patientId));
    }

    private String requirePatientId(User user) {
        boolean patient = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_PATIENT"::equals);
        if (!patient) {
            throw new AccessDeniedException("Only patients may access patient notifications");
        }
        if (user.getLinkedProfileId() == null || user.getLinkedProfileId().isBlank()) {
            throw new IllegalStateException("Patient account has no linked patient profile");
        }
        return user.getLinkedProfileId();
    }

    private void requireOwnPatientId(User user, String patientId) {
        if (!requirePatientId(user).equals(patientId)) {
            throw new AccessDeniedException("Patients may only access their own notifications");
        }
    }
}
