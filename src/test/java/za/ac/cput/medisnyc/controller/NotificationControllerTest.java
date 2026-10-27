package za.ac.cput.medisnyc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import za.ac.cput.medisnyc.domain.Notification;
import za.ac.cput.medisnyc.domain.Role;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.NotificationJpaRepository;
import za.ac.cput.medisnyc.service.NotificationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

    @Test
    void meUsesTheAuthenticatedPatientsLinkedProfileId() {
        NotificationService notifications = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notifications);
        User patient = patient("PAT-A");
        when(notifications.getForPatient("PAT-A")).thenReturn(List.of());

        controller.getMine(patient);

        verify(notifications).getForPatient("PAT-A");
    }

    @Test
    void patientCannotReadAnotherPatientsNotifications() {
        NotificationService notifications = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notifications);

        assertThrows(AccessDeniedException.class,
                () -> controller.getForPatient("PAT-B", patient("PAT-A")));
        verify(notifications, never()).getForPatient("PAT-B");
    }

    @Test
    void patientCannotMarkAnotherPatientsNotificationRead() {
        NotificationJpaRepository repository = mock(NotificationJpaRepository.class);
        Notification otherPatientsNotification = new Notification.Builder()
                .setNotificationId(9L)
                .setPatientId("PAT-B")
                .setPrescriptionId("RX-B")
                .setMessage("Prescription RX-B status changed to Received.")
                .build();
        when(repository.findById(9L)).thenReturn(Optional.of(otherPatientsNotification));
        NotificationController controller = new NotificationController(new NotificationService(repository));

        assertThrows(AccessDeniedException.class, () -> controller.markAsRead(9L, patient("PAT-A")));
        verify(repository, never()).save(otherPatientsNotification);
    }

    @Test
    void staffCannotReadPatientNotifications() {
        NotificationController controller = new NotificationController(mock(NotificationService.class));
        User admin = new User.Builder()
                .setUsername("admin")
                .addRole(new Role.Builder().setName("ROLE_ADMIN").build())
                .build();

        assertThrows(AccessDeniedException.class, () -> controller.getMine(admin));
    }

    @Test
    void unlinkedPatientGetsClearMessage() {
        NotificationController controller = new NotificationController(mock(NotificationService.class));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> controller.getMine(patient(null)));

        assertEquals("Patient account has no linked patient profile", error.getMessage());
    }

    private User patient(String profileId) {
        return new User.Builder()
                .setUsername("patient")
                .setLinkedProfileId(profileId)
                .addRole(new Role.Builder().setName("ROLE_PATIENT").build())
                .build();
    }
}
