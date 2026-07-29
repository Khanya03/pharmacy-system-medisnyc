package za.ac.cput.medisnyc.service;

/* UserManagementService.java
   Module 6: Reports & Administration - User Management (admin only).
*/

import za.ac.cput.medisnyc.domain.Role;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.RoleJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;

    @Autowired
    public UserManagementService(UserJpaRepository userRepository, RoleJpaRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional
    public User setEnabled(Long userId, boolean enabled) {
        User existing = getUser(userId);
        User updated = new User.Builder().copy(existing).setEnabled(enabled).build();
        return userRepository.save(updated);
    }

    @Transactional
    public User assignRole(Long userId, String roleName) {
        User existing = getUser(userId);
        String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
        Role role = roleRepository.findByName(normalized)
                .orElseGet(() -> roleRepository.save(new Role.Builder().setName(normalized).build()));
        User updated = new User.Builder().copy(existing).addRole(role).build();
        return userRepository.save(updated);
    }

    @Transactional
    public void deleteUser(Long userId) {
        getUser(userId);
        userRepository.deleteById(userId);
    }
}
