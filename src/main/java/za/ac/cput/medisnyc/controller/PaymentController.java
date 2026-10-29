package za.ac.cput.medisnyc.controller;

import za.ac.cput.medisnyc.domain.Payment;
import za.ac.cput.medisnyc.domain.PaymentStatus;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.dto.CardPaymentRequest;
import za.ac.cput.medisnyc.dto.CardPaymentResponse;
import za.ac.cput.medisnyc.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/card")
    public ResponseEntity<CardPaymentResponse> payByCard(@RequestBody CardPaymentRequest request,
                                                          @AuthenticationPrincipal User user,
                                                          Authentication authentication) {
        boolean patient = hasRole(authentication, "PATIENT");
        CardPaymentResponse response = paymentService.payByCard(request,
                patient ? patientProfile(user) : null, patient, authentication.getName());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Payment>> getMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getMine(patientProfile(user)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getByOrder(@PathVariable String orderId,
                                                     Authentication authentication,
                                                     @AuthenticationPrincipal User user) {
        String patientId = hasRole(authentication, "PATIENT") ? patientProfile(user) : null;
        return ResponseEntity.ok(paymentService.getByOrder(orderId, patientId));
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<Map<String, Object>> getReceipt(@PathVariable String paymentId,
                                                           Authentication authentication,
                                                           @AuthenticationPrincipal User user) {
        String patientId = hasRole(authentication, "PATIENT") ? patientProfile(user) : null;
        return ResponseEntity.ok(paymentService.getReceipt(paymentId, patientId));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, Object>> refund(@PathVariable String paymentId,
                                                       Authentication authentication) {
        return ResponseEntity.ok(paymentService.refund(paymentId, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<Payment>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.search(from, to, status, page, size, null));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    private String patientProfile(User user) {
        if (user.getLinkedProfileId() == null || user.getLinkedProfileId().isBlank()) {
            throw new AccessDeniedException("Patient account has no linked patient record");
        }
        return user.getLinkedProfileId();
    }
}
