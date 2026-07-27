package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Prescription;
import za.ac.cput.medisnyc.domain.PrescriptionProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionJpaRepository extends JpaRepository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByDoctorId(String doctorId);
    List<Prescription> findByProcessingStatus(PrescriptionProcessingStatus status);
}