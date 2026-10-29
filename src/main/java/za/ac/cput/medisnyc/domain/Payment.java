package za.ac.cput.medisnyc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(
        name = "uk_payment_order_idempotency", columnNames = {"order_id", "idempotency_key"}))
public class Payment {
    @Id
    private String paymentId;
    private String receiptNumber;
    @Column(name = "order_id")
    private String orderId;
    private String patientId;
    private BigDecimal amount;
    private String currency;
    private String method;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String cardBrand;
    private String cardLast4;
    private String cardholderName;
    private String gatewayReference;
    private String failureCode;
    private String failureReason;
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String processedBy;

    protected Payment() {
    }

    private Payment(Builder builder) {
        paymentId = builder.paymentId;
        receiptNumber = builder.receiptNumber;
        orderId = builder.orderId;
        patientId = builder.patientId;
        amount = builder.amount;
        currency = builder.currency;
        method = builder.method;
        status = builder.status;
        cardBrand = builder.cardBrand;
        cardLast4 = builder.cardLast4;
        cardholderName = builder.cardholderName;
        gatewayReference = builder.gatewayReference;
        failureCode = builder.failureCode;
        failureReason = builder.failureReason;
        idempotencyKey = builder.idempotencyKey;
        createdAt = builder.createdAt == null ? LocalDateTime.now() : builder.createdAt;
        paidAt = builder.paidAt;
        refundedAt = builder.refundedAt;
        processedBy = builder.processedBy;
    }

    public String getPaymentId() { return paymentId; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getOrderId() { return orderId; }
    public String getPatientId() { return patientId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public String getCardBrand() { return cardBrand; }
    public String getCardLast4() { return cardLast4; }
    @JsonIgnore
    public String getCardholderName() { return cardholderName; }
    public String getGatewayReference() { return gatewayReference; }
    public String getFailureCode() { return failureCode; }
    public String getFailureReason() { return failureReason; }
    @JsonIgnore
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public String getProcessedBy() { return processedBy; }

    @Override
    public String toString() {
        return "Payment{" + "paymentId='" + paymentId + '\'' + ", receiptNumber='" + receiptNumber
                + '\'' + ", orderId='" + orderId + '\'' + ", amount=" + amount + ", status=" + status
                + ", cardBrand='" + cardBrand + '\'' + ", cardLast4='" + cardLast4 + '\'' + '}';
    }

    public static class Builder {
        private String paymentId, receiptNumber, orderId, patientId, currency, method, cardBrand,
                cardLast4, cardholderName, gatewayReference, failureCode, failureReason,
                idempotencyKey, processedBy;
        private BigDecimal amount;
        private PaymentStatus status;
        private LocalDateTime createdAt, paidAt, refundedAt;

        public Builder setPaymentId(String value) { paymentId = value; return this; }
        public Builder setReceiptNumber(String value) { receiptNumber = value; return this; }
        public Builder setOrderId(String value) { orderId = value; return this; }
        public Builder setPatientId(String value) { patientId = value; return this; }
        public Builder setAmount(BigDecimal value) { amount = value; return this; }
        public Builder setCurrency(String value) { currency = value; return this; }
        public Builder setMethod(String value) { method = value; return this; }
        public Builder setStatus(PaymentStatus value) { status = value; return this; }
        public Builder setCardBrand(String value) { cardBrand = value; return this; }
        public Builder setCardLast4(String value) { cardLast4 = value; return this; }
        public Builder setCardholderName(String value) { cardholderName = value; return this; }
        public Builder setGatewayReference(String value) { gatewayReference = value; return this; }
        public Builder setFailureCode(String value) { failureCode = value; return this; }
        public Builder setFailureReason(String value) { failureReason = value; return this; }
        public Builder setIdempotencyKey(String value) { idempotencyKey = value; return this; }
        public Builder setCreatedAt(LocalDateTime value) { createdAt = value; return this; }
        public Builder setPaidAt(LocalDateTime value) { paidAt = value; return this; }
        public Builder setRefundedAt(LocalDateTime value) { refundedAt = value; return this; }
        public Builder setProcessedBy(String value) { processedBy = value; return this; }

        public Builder copy(Payment payment) {
            paymentId = payment.paymentId; receiptNumber = payment.receiptNumber;
            orderId = payment.orderId; patientId = payment.patientId; amount = payment.amount;
            currency = payment.currency; method = payment.method; status = payment.status;
            cardBrand = payment.cardBrand; cardLast4 = payment.cardLast4;
            cardholderName = payment.cardholderName; gatewayReference = payment.gatewayReference;
            failureCode = payment.failureCode; failureReason = payment.failureReason;
            idempotencyKey = payment.idempotencyKey; createdAt = payment.createdAt;
            paidAt = payment.paidAt; refundedAt = payment.refundedAt; processedBy = payment.processedBy;
            return this;
        }

        public Payment build() {
            if (paymentId == null || orderId == null || patientId == null || amount == null
                    || idempotencyKey == null || idempotencyKey.isBlank() || status == null) {
                throw new IllegalArgumentException("Required payment fields are missing");
            }
            return new Payment(this);
        }
    }
}
