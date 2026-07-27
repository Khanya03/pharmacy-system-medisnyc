package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionItemJpaRepository extends JpaRepository<PrescriptionItem, String> {
    List<PrescriptionItem> findByPrescriptionId(String prescriptionId);
}