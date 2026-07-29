package za.ac.cput.medisnyc.repository.jpa;


import za.ac.cput.medisnyc.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrderId(String orderId);
}