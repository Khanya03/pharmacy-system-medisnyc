package za.ac.cput.medisnyc.controller;

/* ProfileController.java
   Module 1: Profile Page.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProfileService profileService;

    @Autowired
    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(Authentication authentication,
                                                @RequestParam(required = false) String firstName,
                                                @RequestParam(required = false) String lastName,
                                                @RequestParam(required = false) String email) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), firstName, lastName, email));
    }
}