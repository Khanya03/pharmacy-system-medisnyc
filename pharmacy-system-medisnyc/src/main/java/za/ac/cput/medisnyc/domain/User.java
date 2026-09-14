package za.ac.cput.medisnyc.domain;

/* User.java
   User entity for authentication. Implements Spring Security's UserDetails
   so it can plug straight into the authentication flow.
   Author: Lisakhanya Mpahla
*/

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String email;
    private String password; // BCrypt-encoded, never stored/returned in plain text
    private String firstName;
    private String lastName;

    // Links this login account to a domain profile (Patient.medicalId or Doctor.doctorId),
    // depending on the role. Nullable for ADMIN/PHARMACIST accounts with no such profile.
    private String linkedProfileId;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    private User(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.linkedProfileId = builder.linkedProfileId;
        this.enabled = builder.enabled;
        this.roles = builder.roles != null ? builder.roles : new HashSet<>();
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLinkedProfileId() {
        return linkedProfileId;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", linkedProfileId='" + linkedProfileId + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                '}';
    }

    public static class Builder {
        private Long userId;
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String linkedProfileId;
        private boolean enabled = true;
        private Set<Role> roles = new HashSet<>();

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setLinkedProfileId(String linkedProfileId) {
            this.linkedProfileId = linkedProfileId;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setRoles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder addRole(Role role) {
            this.roles.add(role);
            return this;
        }

        public Builder copy(User user) {
            this.userId = user.userId;
            this.username = user.username;
            this.email = user.email;
            this.password = user.password;
            this.firstName = user.firstName;
            this.lastName = user.lastName;
            this.linkedProfileId = user.linkedProfileId;
            this.enabled = user.enabled;
            this.roles = new HashSet<>(user.roles);
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
