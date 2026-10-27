package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.domain.Role;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {

    @Test
    void linksLegacyPatientAccountToPatientProfileByEmail() {
        User user = patientUser();
        Patient profile = new Patient.Builder()
                .setMedicalId("PAT-1")
                .setFirstName("A")
                .setLastName("Patient")
                .setEmail("patient@example.test")
                .build();
        UserJpaRepository users = mock(UserJpaRepository.class);
        when(users.findByUsername("patient")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(patients.existsById(any())).thenReturn(false);
        when(patients.findAll()).thenReturn(List.of(profile));

        User linked = new UserProfileService(users, patients,
                mock(LegacyPrescriptionPatientIdMigration.class)).getProfile("patient");

        assertEquals("PAT-1", linked.getLinkedProfileId());
    }

    @Test
    void createsMissingPatientProfileWhenLegacyAccountHasNoMatchingPatient() {
        User user = patientUser();
        UserJpaRepository users = mock(UserJpaRepository.class);
        when(users.findByUsername("patient")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PatientJpaRepository patients = mock(PatientJpaRepository.class);
        when(patients.findAll()).thenReturn(List.of());
        when(patients.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User linked = new UserProfileService(users, patients,
                mock(LegacyPrescriptionPatientIdMigration.class)).getProfile("patient");

        assertTrue(linked.getLinkedProfileId().startsWith("PAT"));
    }

    private User patientUser() {
        return new User.Builder()
                .setUsername("patient")
                .setEmail("patient@example.test")
                .setFirstName("A")
                .setLastName("Patient")
                .addRole(new Role.Builder().setName("ROLE_PATIENT").build())
                .build();
    }
}
