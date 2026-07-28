package za.ac.cput.medisnyc.controller;

/* AdminController.java
   Module 6: Reports & Administration - User Management API / Audit Log.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.domain.AuditLog;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.service.AuditLogService;
import za.ac.cput.medisnyc.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserManagementService userManagementService;
    private final AuditLogService auditLogService;

    @Autowired
    public AdminController(UserManagementService userManagementService, AuditLogService auditLogService) {
        this.userManagementService = userManagementService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementService.getUser(userId));
    }

    @PutMapping("/users/{userId}/enabled")
    public ResponseEntity<User> setEnabled(@PathVariable Long userId, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userManagementService.setEnabled(userId, enabled));
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<User> assignRole(@PathVariable Long userId, @RequestParam String role) {
        return ResponseEntity.ok(userManagementService.assignRole(userId, role));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAll());
    }
}