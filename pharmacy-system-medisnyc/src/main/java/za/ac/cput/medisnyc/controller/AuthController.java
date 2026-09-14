package za.ac.cput.medisnyc.controller;

/* AuthController.java
   Module 1: Login API, Registration API, Forgot/Reset Password.
   Author: Lisakhanya Mpahla
*/

import jakarta.validation.Valid;
import za.ac.cput.medisnyc.dto.ApiResponse;
import za.ac.cput.medisnyc.dto.AuthResponse;
import za.ac.cput.medisnyc.dto.ForgotPasswordRequest;
import za.ac.cput.medisnyc.dto.LoginRequest;
import za.ac.cput.medisnyc.dto.RegisterRequest;
import za.ac.cput.medisnyc.dto.ResetPasswordRequest;
import za.ac.cput.medisnyc.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.createPasswordResetToken(request.getEmail());
        // Demo only: normally this token is emailed, never returned in the API response.
        return ResponseEntity.ok(new ApiResponse(true, "Reset token generated: " + token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully"));
    }
}
