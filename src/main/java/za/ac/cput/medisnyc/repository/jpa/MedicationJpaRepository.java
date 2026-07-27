package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicationJpaRepository extends JpaRepository<Medication, String> {
    List<Medication> findByCategoryIgnoreCase(String category);
    List<Medication> findByMedicationNameContainingIgnoreCase(String name);
}