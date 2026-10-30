package za.ac.cput.medisnyc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.cput.medisnyc.dto.CardPaymentRequest;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatedCardGatewayTest {
    private SimulatedCardGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new SimulatedCardGateway();
        org.springframework.test.util.ReflectionTestUtils.setField(gateway, "mode", "demo");
    }

    @Test
    void validatesLuhnAndExpiry() {
        assertTrue(SimulatedCardGateway.passesLuhn("4242424242424242"));
        assertFalse(SimulatedCardGateway.passesLuhn("4242424242424241"));
        CardPaymentRequest expired = request("4242424242424242");
        expired.setExpiryMonth(1);
        expired.setExpiryYear(YearMonth.now().minusMonths(1).getYear());
        assertEquals("EXPIRED_CARD", gateway.charge(expired).failureCode());
    }

    @Test
    void eachDemoTestCardHasItsContractOutcome() {
        assertTrue(gateway.charge(request("4242424242424242")).approved());
        assertTrue(gateway.charge(request("5555555555554444")).approved());
        assertEquals("CARD_DECLINED", gateway.charge(request("4000000000000002")).failureCode());
        assertEquals("INSUFFICIENT_FUNDS",
                gateway.charge(request("4000000000009995")).failureCode());
        assertEquals("EXPIRED_CARD", gateway.charge(request("4000000000000069")).failureCode());
    }

    @Test
    void demoApprovesAnyOtherCardNumber() {
        var result = gateway.charge(request("1234567890123456"));
        assertTrue(result.approved());
        assertEquals("3456", result.cardLast4());
    }

    @Test
    void rejectsInvalidCvvAndDetectsSupportedBrands() {
        CardPaymentRequest invalidCvv = request("4242424242424242");
        invalidCvv.setCvv("12");
        assertEquals("INVALID_CARD", gateway.charge(invalidCvv).failureCode());
        assertEquals("Visa", SimulatedCardGateway.detectBrand("4242424242424242"));
        assertEquals("Mastercard", SimulatedCardGateway.detectBrand("5555555555554444"));
        assertEquals("Amex", SimulatedCardGateway.detectBrand("378282246310005"));
    }

    private CardPaymentRequest request(String number) {
        CardPaymentRequest request = new CardPaymentRequest();
        request.setCardholderName("Test Patient");
        request.setCardNumber(number);
        request.setExpiryMonth(12);
        request.setExpiryYear(YearMonth.now().getYear() + 2);
        request.setCvv("123");
        return request;
    }
}
