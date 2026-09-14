package za.ac.cput.medisnyc.security;

/* CustomUserDetailsService.java
   Loads a User (and its Roles) for Spring Security during authentication.
   Author: Lisakhanya Mpahla
*/

import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user; // User implements UserDetails
    }
}
