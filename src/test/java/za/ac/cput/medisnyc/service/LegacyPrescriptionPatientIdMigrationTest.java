package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import za.ac.cput.medisnyc.domain.PrescriptionStatus;
import za.ac.cput.medisnyc.domain.Role;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegacyPrescriptionPatientIdMigrationTest {

    @Test
    void migratesUsernamePatientIdIdempotentlyWhenProfileIsLinked() {
        AtomicReference<Prescription> stored = new AtomicReference<>(prescription("khanya"));
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findByPatientId("khanya")).thenAnswer(invocation ->
                stored.get() == null || !"khanya".equals(stored.get().getPatientId())
                        ? List.of() : List.of(stored.get()));
        when(prescriptions.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            stored.set(saved);
            return saved;
        });
        UserJpaRepository users = mock(UserJpaRepository.class);
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(patients.existsById("PAT-1")).thenReturn(true);
        LegacyPrescriptionPatientIdMigration migration =
                new LegacyPrescriptionPatientIdMigration(prescriptions, users, patients);
        User user = patientUser("PAT-1", 1L);

        migration.migrateForPatient(user);
        migration.migrateForPatient(user);

        assertEquals("PAT-1", stored.get().getPatientId());
        verify(prescriptions, times(1)).save(any(Prescription.class));
    }

    @Test
    void leavesPrescriptionUnchangedWhenUsernameMatchesMultipleAccounts() {
        Prescription legacy = prescription("khanya");
        PrescriptionJpaRepository prescriptions = mock(PrescriptionJpaRepository.class);
        when(prescriptions.findAll()).thenReturn(List.of(legacy));
        UserJpaRepository users = mock(UserJpaRepository.class);
        when(users.findAllByUsername("khanya"))
                .thenReturn(List.of(patientUser("PAT-1", 1L), patientUser("PAT-2", 2L)));
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(patients.existsById("khanya")).thenReturn(false);
        LegacyPrescriptionPatientIdMigration migration =
                new LegacyPrescriptionPatientIdMigration(prescriptions, users, patients);

        migration.run();

        verify(prescriptions, never()).findByPatientId("khanya");
        verify(prescriptions, never()).save(any(Prescription.class));
        assertEquals("khanya", legacy.getPatientId());
    }

    private Prescription prescription(String patientId) {
        return new Prescription.Builder()
                .setPrescriptionId("RX-1")
                .setPatientId(patientId)
                .setDoctorId("DOC-1")
                .setDateIssued(LocalDate.now())
                .setExpiryDate(LocalDate.now().plusDays(30))
                .setStatus(PrescriptionStatus.ACTIVE)
                .setProcessingStatus(PrescriptionProcessingStatus.PENDING)
                .setCreatedAt(LocalDateTime.now())
                .build();
    }

    private User patientUser(String profileId, Long userId) {
        return new User.Builder()
                .setUserId(userId)
                .setUsername("khanya")
                .setLinkedProfileId(profileId)
                .addRole(new Role.Builder().setName("ROLE_PATIENT").build())
                .build();
    }
}
