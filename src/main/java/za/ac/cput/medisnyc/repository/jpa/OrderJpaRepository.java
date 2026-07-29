package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    List<Order> findByPatientId(String patientId);
    List<Order> findByDispensingStatus(OrderStatus status);
}