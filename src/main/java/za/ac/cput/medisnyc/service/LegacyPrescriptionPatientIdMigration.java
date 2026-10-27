package za.ac.cput.medisnyc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PrescriptionJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;

@Component
@Order(3)
public class LegacyPrescriptionPatientIdMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LegacyPrescriptionPatientIdMigration.class);

    private final PrescriptionJpaRepository prescriptionRepository;
    private final UserJpaRepository userRepository;
    private final PatientJpaRepository patientRepository;

    public LegacyPrescriptionPatientIdMigration(PrescriptionJpaRepository prescriptionRepository,
                                               UserJpaRepository userRepository,
                                               PatientJpaRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Prescription prescription : prescriptionRepository.findAll()) {
            String legacyPatientId = prescription.getPatientId();
            if (legacyPatientId == null || legacyPatientId.isBlank()
                    || legacyPatientId.regionMatches(true, 0, "PAT", 0, 3)
                    || patientRepository.existsById(legacyPatientId)) {
                continue;
            }

            var matchingUsers = userRepository.findAllByUsername(legacyPatientId);
            if (matchingUsers.isEmpty()) {
                logger.warn("Skipping prescription {} with non-profile patient ID {}; no matching user exists",
                        prescription.getPrescriptionId(), legacyPatientId);
                continue;
            }
            if (matchingUsers.size() > 1) {
                logger.warn("Skipping prescription {} with patient ID {}; multiple matching accounts exist",
                        prescription.getPrescriptionId(), legacyPatientId);
                continue;
            }

            User user = matchingUsers.get(0);
            if (!isPatient(user) || user.getLinkedProfileId() == null
                    || user.getLinkedProfileId().isBlank()
                    || !patientRepository.existsById(user.getLinkedProfileId())) {
                logger.warn("Skipping prescription {} with patient ID {}; the matching account has no unique, "
                                + "valid patient profile",
                        prescription.getPrescriptionId(), legacyPatientId);
                continue;
            }
            migrateForPatient(user);
        }
    }

    @Transactional
    public void migrateForPatient(User user) {
        String linkedProfileId = user.getLinkedProfileId();
        if (!isPatient(user) || linkedProfileId == null || linkedProfileId.isBlank()
                || !patientRepository.existsById(linkedProfileId)) {
            logger.warn("Skipping legacy prescription migration for account {}; no valid patient profile is linked",
                    user.getUsername());
            return;
        }

        for (Prescription prescription : prescriptionRepository.findByPatientId(user.getUsername())) {
            prescriptionRepository.save(new Prescription.Builder()
                    .setPrescriptionId(prescription.getPrescriptionId())
                    .setPatientId(linkedProfileId)
                    .setDoctorId(prescription.getDoctorId())
                    .setDateIssued(prescription.getDateIssued())
                    .setExpiryDate(prescription.getExpiryDate())
                    .setInstructions(prescription.getInstructions())
                    .setRefillsAllowed(prescription.getRefillsAllowed())
                    .setRefillsUsed(prescription.getRefillsUsed())
                    .setStatus(prescription.getStatus())
                    .setProcessingStatus(prescription.getProcessingStatus())
                    .setCreatedAt(prescription.getCreatedAt())
                    .build());
            logger.info("Migrated prescription {} patient ID from username {} to profile {}",
                    prescription.getPrescriptionId(), user.getUsername(), linkedProfileId);
        }
    }

    private boolean isPatient(User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_PATIENT"::equals);
    }
}
