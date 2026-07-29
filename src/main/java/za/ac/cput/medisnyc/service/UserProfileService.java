package za.ac.cput.medisnyc.service;

/* UserProfileService.java
   Module 1: Profile Page - view/update the logged-in user's own profile.
*/

import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserJpaRepository userRepository;

    @Autowired
    public UserProfileService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional
    public User updateProfile(String username, String firstName, String lastName, String email) {
        User user = getProfile(username);
        User updated = new User.Builder().copy(user)
                .setFirstName(firstName != null ? firstName : user.getFirstName())
                .setLastName(lastName != null ? lastName : user.getLastName())
                .setEmail(email != null ? email : user.getEmail())
                .build();
        return userRepository.save(updated);
    }
}
