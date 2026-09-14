package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryJpaRepository extends JpaRepository<Inventory, String> {
    List<Inventory> findByMedicationId(String medicationId);
    List<Inventory> findByStockLevelLessThanEqual(int reorderLevel);
}