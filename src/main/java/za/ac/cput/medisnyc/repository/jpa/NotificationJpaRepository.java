package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<Notification> findByPatientIdAndIsReadFalse(String patientId);
}
