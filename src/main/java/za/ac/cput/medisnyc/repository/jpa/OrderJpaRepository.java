package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import za.ac.cput.medisnyc.domain.OrderPaymentStatus;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    List<Order> findByPatientId(String patientId);
    java.util.Optional<Order> findFirstByPrescriptionId(String prescriptionId);
    List<Order> findByDispensingStatus(OrderStatus status);
    boolean existsByPatientId(String patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderId = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") String orderId);

    @Query("select count(o) from Order o where o.dispensingStatus <> za.ac.cput.medisnyc.domain.OrderStatus.CANCELLED "
            + "and (o.paymentStatus is null or o.paymentStatus = :status)")
    long countUnpaidOrders(@Param("status") OrderPaymentStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where "
            + "o.dispensingStatus <> za.ac.cput.medisnyc.domain.OrderStatus.CANCELLED "
            + "and (o.paymentStatus is null or o.paymentStatus = :status)")
    BigDecimal sumOutstandingAmount(@Param("status") OrderPaymentStatus status);
}