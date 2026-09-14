package za.ac.cput.medisnyc.domain;


/* AuditLog.java
   AuditLog entity - Module 6: Reports & Administration Module.
   Author: Thakane Jeanet Moloi
*/

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String username;
    private String action;
    private String entityAffected;
    private String details;
    private LocalDateTime timestamp;

    protected AuditLog() {
    }

    private AuditLog(Builder builder) {
        this.logId = builder.logId;
        this.username = builder.username;
        this.action = builder.action;
        this.entityAffected = builder.entityAffected;
        this.details = builder.details;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    }

    public Long getLogId() { return logId; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getEntityAffected() { return entityAffected; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(logId, auditLog.logId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", entityAffected='" + entityAffected + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class Builder {
        private Long logId;
        private String username;
        private String action;
        private String entityAffected;
        private String details;
        private LocalDateTime timestamp;

        public Builder setLogId(Long logId) {
            this.logId = logId;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public Builder setEntityAffected(String entityAffected) {
            this.entityAffected = entityAffected;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}