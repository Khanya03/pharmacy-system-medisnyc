package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientJpaRepository extends JpaRepository<Patient, String> {
    Optional<Patient> findByEmailIgnoreCase(String email);
}
