package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.PrescriptionCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionCollectionJpaRepository extends JpaRepository<PrescriptionCollection, String> {
    List<PrescriptionCollection> findByPatientId(String patientId);
}