package za.ac.cput.medisnyc.service;

/* AuditLogService.java
   Module 6: Reports & Administration - Audit Log.
*/

import za.ac.cput.medisnyc.domain.AuditLog;
import za.ac.cput.medisnyc.repository.jpa.AuditLogJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogJpaRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogJpaRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String username, String action, String entityAffected, String details) {
        AuditLog entry = new AuditLog.Builder()
                .setUsername(username)
                .setAction(action)
                .setEntityAffected(entityAffected)
                .setDetails(details)
                .build();
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getAll() {
        return auditLogRepository.findAll();
    }
}
