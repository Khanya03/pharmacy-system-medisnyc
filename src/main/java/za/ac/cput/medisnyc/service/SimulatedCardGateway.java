package za.ac.cput.medisnyc.service;

import za.ac.cput.medisnyc.dto.CardPaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.UUID;

@Component
public class SimulatedCardGateway implements PaymentGateway {
    @Value("${app.payments.mode:demo}")
    private String mode;

    @Override
    public GatewayResult charge(CardPaymentRequest request) {
        String number = request.getCardNumber() == null
                ? "" : request.getCardNumber().replaceAll("[ -]", "");
        String brand = detectBrand(number);
        String last4 = number.length() >= 4 ? number.substring(number.length() - 4) : null;
        String reference = "SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        if (request.getCardholderName() == null || request.getCardholderName().isBlank()) {
            return failed(brand, last4, reference, "INVALID_CARD", "Cardholder name is required");
        }
        if (!number.matches("\\d{12,19}")) {
            return failed(brand, last4, reference, "INVALID_CARD", "Card number must be 12 to 19 digits");
        }
        if (brand == null) {
            brand = "Demo";
        }
        if (request.getExpiryMonth() < 1 || request.getExpiryMonth() > 12
                || request.getExpiryYear() < YearMonth.now().getYear()
                || request.getExpiryMonth() < YearMonth.now().getMonthValue()
                && request.getExpiryYear() == YearMonth.now().getYear()) {
            return failed(brand, last4, reference, "EXPIRED_CARD", "Card has expired");
        }
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
            return failed(brand, last4, reference, "INVALID_CARD", "CVV must be 3 or 4 digits");
        }

        if ("demo".equalsIgnoreCase(mode)) {
            String code = switch (number) {
                case "4000000000000002" -> "CARD_DECLINED";
                case "4000000000009995" -> "INSUFFICIENT_FUNDS";
                case "4000000000000069" -> "EXPIRED_CARD";
                default -> null;
            };
            if (code != null) {
                String reason = switch (code) {
                    case "CARD_DECLINED" -> "Card was declined";
                    case "INSUFFICIENT_FUNDS" -> "Insufficient funds";
                    default -> "Card has expired";
                };
                return failed(brand, last4, reference, code, reason);
            }
        }
        return new GatewayResult(true, brand, last4, reference, null, null);
    }

    public static boolean passesLuhn(String number) {
        if (number == null || !number.matches("\\d+")) return false;
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit && (digit *= 2) > 9) digit -= 9;
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    public static String detectBrand(String number) {
        if (number == null) return null;
        if (number.matches("^4\\d{12}(?:\\d{3})?(?:\\d{3})?$")) return "Visa";
        if (number.matches("^(5[1-5]\\d{14}|2(?:2[2-9]|[3-6]\\d|7[01]|720)\\d{12})$")) {
            return "Mastercard";
        }
        if (number.matches("^3[47]\\d{13}$")) return "Amex";
        return null;
    }

    private GatewayResult failed(String brand, String last4, String reference, String code, String reason) {
        return new GatewayResult(false, brand, last4, reference, code, reason);
    }
}
