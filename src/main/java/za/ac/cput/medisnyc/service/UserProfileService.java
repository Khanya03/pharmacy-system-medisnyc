package za.ac.cput.medisnyc.service;

/* UserProfileService.java
   Module 1: Profile Page - view/update the logged-in user's own profile.
*/

import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.domain.Patient;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    private final UserJpaRepository userRepository;
    private final PatientJpaRepository patientRepository;
    private final LegacyPrescriptionPatientIdMigration prescriptionPatientIdMigration;

    @Autowired
    public UserProfileService(UserJpaRepository userRepository, PatientJpaRepository patientRepository,
                              LegacyPrescriptionPatientIdMigration prescriptionPatientIdMigration) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.prescriptionPatientIdMigration = prescriptionPatientIdMigration;
    }

    @Transactional
    public User getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!isPatient(user)) {
            return user;
        }
        if (user.getLinkedProfileId() != null && patientRepository.existsById(user.getLinkedProfileId())) {
            prescriptionPatientIdMigration.migrateForPatient(user);
            return user;
        }
        if (user.getLinkedProfileId() != null) {
            logger.warn("Patient account {} references missing profile {}; resolving by email",
                    user.getUsername(), user.getLinkedProfileId());
        }

        List<Patient> matchingPatients = patientRepository.findAll().stream()
                .filter(patient -> patient.getEmail() != null
                        && user.getEmail() != null
                        && patient.getEmail().equalsIgnoreCase(user.getEmail()))
                .toList();
        if (matchingPatients.size() > 1) {
            throw new IllegalStateException("Multiple patient profiles match this account; contact support");
        }

        Patient patient;
        if (matchingPatients.size() == 1) {
            patient = matchingPatients.get(0);
        } else {
            if (user.getFirstName() == null || user.getFirstName().isBlank()
                    || user.getLastName() == null || user.getLastName().isBlank()) {
                throw new IllegalStateException("Patient account has no linked patient profile; contact support");
            }
            patient = new Patient.Builder()
                    .setMedicalId("PAT" + UUID.randomUUID().toString().replace("-", ""))
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setEmail(user.getEmail())
                    .build();
            patient = patientRepository.save(patient);
            logger.info("Created missing patient profile {} for account {}",
                    patient.getMedicalId(), user.getUsername());
        }

        User linkedUser = userRepository.save(new User.Builder().copy(user)
                .setLinkedProfileId(patient.getMedicalId())
                .build());
        prescriptionPatientIdMigration.migrateForPatient(linkedUser);
        return linkedUser;
    }

    @Transactional
    public User updateProfile(String username, String firstName, String lastName, String email) {
        User user = getProfile(username);
        User updated = new User.Builder().copy(user)
                .setFirstName(firstName != null ? firstName : user.getFirstName())
                .setLastName(lastName != null ? lastName : user.getLastName())
                .setEmail(email != null ? email : user.getEmail())
                .build();
        return userRepository.save(updated);
    }

    private boolean isPatient(User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_PATIENT"::equals);
    }
}
