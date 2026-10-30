package za.ac.cput.medisnyc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.dto.CardPaymentRequest;
import za.ac.cput.medisnyc.dto.CardPaymentResponse;
import za.ac.cput.medisnyc.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Test
    void patientPaymentUsesLinkedProfileAndMarksPatientActor() {
        PaymentService service = mock(PaymentService.class);
        CardPaymentResponse response = new CardPaymentResponse("PAY-1", "RCP-1", "ORD-1",
                new BigDecimal("25.00"), "ZAR", "PAID", "Visa", "4242", LocalDateTime.now());
        when(service.payByCard(any(), eq("PAT-1"), eq(true), eq("patient1"))).thenReturn(response);
        PaymentController controller = new PaymentController(service);

        var result = controller.payByCard(new CardPaymentRequest(), patient("PAT-1"), patientAuth("patient1"));

        assertEquals(201, result.getStatusCode().value());
        assertEquals("PAY-1", result.getBody().paymentId());
        verify(service).payByCard(any(), eq("PAT-1"), eq(true), eq("patient1"));
    }

    @Test
    void staffPaymentDoesNotForcePatientOwnership() {
        PaymentService service = mock(PaymentService.class);
        CardPaymentResponse response = new CardPaymentResponse("PAY-2", "RCP-2", "ORD-2",
                new BigDecimal("40.00"), "ZAR", "PAID", "Mastercard", "4444", LocalDateTime.now());
        when(service.payByCard(any(), isNull(), eq(false), eq("pharmacist1"))).thenReturn(response);
        PaymentController controller = new PaymentController(service);

        var result = controller.payByCard(new CardPaymentRequest(), staff("pharmacist1"), staffAuth("pharmacist1"));

        assertEquals(201, result.getStatusCode().value());
        verify(service).payByCard(any(), isNull(), eq(false), eq("pharmacist1"));
    }

    @Test
    void patientWithoutLinkedProfileCannotPay() {
        PaymentController controller = new PaymentController(mock(PaymentService.class));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> controller.payByCard(new CardPaymentRequest(), patient(null), patientAuth("patient1")));

        assertTrue(ex.getMessage().contains("linked patient record"));
    }

    private static User patient(String profileId) {
        return new User.Builder()
                .setUsername("patient1")
                .setEmail("patient@example.com")
                .setPassword("encoded")
                .setLinkedProfileId(profileId)
                .build();
    }

    private static User staff(String username) {
        return new User.Builder()
                .setUsername(username)
                .setEmail(username + "@example.com")
                .setPassword("encoded")
                .build();
    }

    private static UsernamePasswordAuthenticationToken patientAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    private static UsernamePasswordAuthenticationToken staffAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("ROLE_PHARMACIST")));
    }
}
