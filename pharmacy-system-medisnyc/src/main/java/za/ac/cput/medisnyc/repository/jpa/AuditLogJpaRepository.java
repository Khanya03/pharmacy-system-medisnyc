package za.ac.cput.medisnyc.repository.jpa;

import za.ac.cput.medisnyc.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
}
