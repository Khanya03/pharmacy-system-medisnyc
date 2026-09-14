package za.ac.cput.medisnyc.domain;

/* PasswordResetToken.java
   Short-lived token issued for the Forgot Password flow (Module 1).
   Author: Lisakhanya Mpahla
*/

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String username;
    private LocalDateTime expiryDate;
    private boolean used;

    protected PasswordResetToken() {
    }

    private PasswordResetToken(Builder builder) {
        this.id = builder.id;
        this.token = builder.token;
        this.username = builder.username;
        this.expiryDate = builder.expiryDate;
        this.used = builder.used;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public boolean isUsed() { return used; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public static class Builder {
        private Long id;
        private String token;
        private String username;
        private LocalDateTime expiryDate;
        private boolean used = false;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setExpiryDate(LocalDateTime expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder setUsed(boolean used) {
            this.used = used;
            return this;
        }

        public PasswordResetToken build() {
            return new PasswordResetToken(this);
        }
    }
}
