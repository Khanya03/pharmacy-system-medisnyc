package za.ac.cput.medisnyc.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PaymentTest {
    @Test
    void paymentHasNoPanOrCvvFieldAndToStringDoesNotExposeCardData() {
        for (Field field : Payment.class.getDeclaredFields()) {
            assertFalse(field.getName().equalsIgnoreCase("cardNumber"));
            assertFalse(field.getName().equalsIgnoreCase("cvv"));
            assertFalse(field.getName().equalsIgnoreCase("pan"));
        }
        Payment payment = new Payment.Builder().setPaymentId("PAY-12345678")
                .setOrderId("ORD-1").setPatientId("PAT-1").setAmount(new BigDecimal("10.00"))
                .setIdempotencyKey("key").setStatus(PaymentStatus.PAID)
                .setCardBrand("Visa").setCardLast4("4242").build();
        assertFalse(payment.toString().contains("4242424242424242"));
        assertFalse(payment.toString().toLowerCase().contains("cvv"));
    }
}
