package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorJpaRepository extends JpaRepository<Doctor, String> {
    List<Doctor> findBySpecializationIgnoreCase(String specialization);
    List<Doctor> findByLastNameIgnoreCase(String lastName);
}
