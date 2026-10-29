package za.ac.cput.medisnyc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardPaymentResponse(String paymentId, String receiptNumber, String orderId,
                                  BigDecimal amount, String currency, String status,
                                  String cardBrand, String cardLast4, LocalDateTime paidAt) {}
