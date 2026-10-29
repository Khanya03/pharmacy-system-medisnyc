package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Payment;
import za.ac.cput.medisnyc.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderIdAndIdempotencyKey(String orderId, String idempotencyKey);
    List<Payment> findByOrderIdOrderByCreatedAtDesc(String orderId);
    List<Payment> findByPatientIdOrderByCreatedAtDesc(String patientId);
    long countByOrderIdAndStatusAndCreatedAtAfter(String orderId, PaymentStatus status, LocalDateTime after);
    long countByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);

    @Query("select p from Payment p where (:patientId is null or p.patientId = :patientId) "
            + "and (:status is null or p.status = :status) "
            + "and (:fromDate is null or p.createdAt >= :fromDate) "
            + "and (:toDate is null or p.createdAt < :toDate)")
    Page<Payment> search(@Param("patientId") String patientId, @Param("status") PaymentStatus status,
                         @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate,
                         Pageable pageable);

    @Query("select coalesce(sum(case when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.PAID "
            + "or p.status = za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED then p.amount else 0 end), 0), "
            + "coalesce(sum(case when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED "
            + "then p.amount else 0 end), 0), "
            + "sum(case when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.PAID then 1 else 0 end) "
            + "from Payment p where p.createdAt is not null "
            + "and (:fromDate is null or p.paidAt >= :fromDate) "
            + "and (:toDate is null or p.paidAt < :toDate)")
    Object[] revenueTotals(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query("select function('DATE', p.paidAt), "
            + "sum(case when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.PAID then p.amount "
            + "when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED then -p.amount else 0 end) "
            + "from Payment p where p.status in (za.ac.cput.medisnyc.domain.PaymentStatus.PAID, "
            + "za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED) and p.paidAt >= :fromDate and p.paidAt < :toDate "
            + "group by function('DATE', p.paidAt) order by function('DATE', p.paidAt)")
    List<Object[]> revenueByDay(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query("select p.cardBrand, "
            + "sum(case when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.PAID then p.amount "
            + "when p.status = za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED then -p.amount else 0 end), "
            + "count(p) from Payment p where p.status in (za.ac.cput.medisnyc.domain.PaymentStatus.PAID, "
            + "za.ac.cput.medisnyc.domain.PaymentStatus.REFUNDED) and p.paidAt >= :fromDate and p.paidAt < :toDate "
            + "group by p.cardBrand")
    List<Object[]> revenueByCardBrand(@Param("fromDate") LocalDateTime fromDate,
                                      @Param("toDate") LocalDateTime toDate);

    long countByStatus(PaymentStatus status);

    @Query("select count(p) from Payment p where p.status = :status "
            + "and (:fromDate is null or p.createdAt >= :fromDate) "
            + "and (:toDate is null or p.createdAt < :toDate)")
    long countByStatusAndCreatedAtRange(@Param("status") PaymentStatus status,
                                        @Param("fromDate") LocalDateTime fromDate,
                                        @Param("toDate") LocalDateTime toDate);

    @Query("select count(p) from Payment p where p.status = :status "
            + "and p.paidAt >= :fromDate and p.paidAt < :toDate")
    long countByStatusAndPaidAtRange(@Param("status") PaymentStatus status,
                                     @Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate);

    Optional<Payment> findFirstByPaidAtNotNullOrderByPaidAtAsc();

    @Query("select p from Payment p where (:fromDate is null or p.createdAt >= :fromDate) "
            + "and (:toDate is null or p.createdAt < :toDate) order by p.createdAt desc")
    List<Payment> findForExport(@Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate);
}
