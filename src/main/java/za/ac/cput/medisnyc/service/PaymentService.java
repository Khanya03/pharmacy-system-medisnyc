package za.ac.cput.medisnyc.service;

import za.ac.cput.medisnyc.domain.*;
import za.ac.cput.medisnyc.dto.CardPaymentRequest;
import za.ac.cput.medisnyc.dto.CardPaymentResponse;
import za.ac.cput.medisnyc.exception.PaymentException;
import za.ac.cput.medisnyc.exception.ResourceNotFoundException;
import za.ac.cput.medisnyc.repository.jpa.MedicationJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderItemJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.OrderJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PatientJpaRepository;
import za.ac.cput.medisnyc.repository.jpa.PaymentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final PatientJpaRepository patientRepository;
    private final MedicationJpaRepository medicationRepository;
    private final PaymentGateway gateway;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    @Value("${app.payments.vat-rate:0.15}")
    private BigDecimal vatRate = new BigDecimal("0.15");

    public PaymentService(PaymentJpaRepository paymentRepository, OrderJpaRepository orderRepository,
                          OrderItemJpaRepository orderItemRepository, PatientJpaRepository patientRepository,
                          MedicationJpaRepository medicationRepository, PaymentGateway gateway,
                          NotificationService notificationService, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.patientRepository = patientRepository;
        this.medicationRepository = medicationRepository;
        this.gateway = gateway;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(noRollbackFor = PaymentException.class)
    public CardPaymentResponse payByCard(CardPaymentRequest request, String actorId,
                                         boolean patientActor, String processedBy) {
        try {
            if (request.getOrderId() == null || request.getOrderId().isBlank()
                    || request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
                throw new IllegalArgumentException("Order ID and idempotency key are required");
            }
            Order order = orderRepository.findByIdForUpdate(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));
            if (patientActor && (actorId == null || !actorId.equals(order.getPatientId()))) {
                throw new AccessDeniedException("Patients may only pay their own orders");
            }

            Payment previous = paymentRepository.findByOrderIdAndIdempotencyKey(
                    order.getOrderId(), request.getIdempotencyKey()).orElse(null);
            if (previous != null) {
                if (previous.getStatus() == PaymentStatus.FAILED) {
                    throw new PaymentException(HttpStatus.PAYMENT_REQUIRED,
                            previous.getFailureCode(), previous.getFailureReason());
                }
                return toResponse(previous);
            }
            if (order.getPaymentStatus() == OrderPaymentStatus.PAID) {
                throw conflict("Order is already paid");
            }
            if (order.getDispensingStatus() == OrderStatus.CANCELLED) {
                throw conflict("Cancelled orders cannot be paid");
            }
            if (orderItemRepository.findByOrderId(order.getOrderId()).isEmpty()
                    || order.getTotalAmount() == null
                    || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw conflict("There is nothing to pay for this order");
            }
            if (paymentRepository.countByOrderIdAndStatusAndCreatedAtAfter(
                    order.getOrderId(), PaymentStatus.FAILED, LocalDateTime.now().minusMinutes(10)) >= 5) {
                throw new PaymentException(HttpStatus.TOO_MANY_REQUESTS, null,
                        "Too many failed payment attempts; try again in 10 minutes");
            }

            PaymentGateway.GatewayResult result = gateway.charge(request);
            LocalDateTime now = LocalDateTime.now();
            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String receiptNumber = nextReceiptNumber(now);
            Payment payment = new Payment.Builder()
                    .setPaymentId(paymentId)
                    .setReceiptNumber(receiptNumber)
                    .setOrderId(order.getOrderId())
                    .setPatientId(order.getPatientId())
                    .setAmount(order.getTotalAmount())
                    .setCurrency("ZAR")
                    .setMethod("CARD")
                    .setStatus(result.approved() ? PaymentStatus.PAID : PaymentStatus.FAILED)
                    .setCardBrand(result.cardBrand())
                    .setCardLast4(result.cardLast4())
                    .setCardholderName(request.getCardholderName())
                    .setGatewayReference(result.gatewayReference())
                    .setFailureCode(result.failureCode())
                    .setFailureReason(result.failureReason())
                    .setIdempotencyKey(request.getIdempotencyKey())
                    .setCreatedAt(now)
                    .setPaidAt(result.approved() ? now : null)
                    .setProcessedBy(processedBy)
                    .build();
            paymentRepository.save(payment);

            if (!result.approved()) {
                throw new PaymentException(HttpStatus.PAYMENT_REQUIRED,
                        result.failureCode(), result.failureReason());
            }

            orderRepository.save(copyOrder(order, OrderPaymentStatus.PAID, paymentId, now));
            notificationService.notify(order.getPatientId(), order.getPrescriptionId(),
                    receiptNotification(payment, order));
            auditLogService.log(processedBy, "PAYMENT_RECEIVED", paymentId,
                    "Payment received for order " + order.getOrderId() + ", amount R" + order.getTotalAmount());
            return toResponse(payment);
        } finally {
            request.clearCardData();
        }
    }


    private String receiptNotification(Payment payment, Order order) {
        String when = payment.getPaidAt() == null ? "" : payment.getPaidAt().toLocalDate().toString();
        String card = (payment.getCardBrand() == null ? "Card" : payment.getCardBrand())
                + " ending " + (payment.getCardLast4() == null ? "****" : payment.getCardLast4());
        return "Receipt " + payment.getReceiptNumber()
                + ": Payment of R" + order.getTotalAmount()
                + " received for order " + order.getOrderId()
                + (when.isBlank() ? "" : " on " + when)
                + ". " + card + ". Status: PAID.";
    }

    @Transactional
    public Map<String, Object> refund(String paymentId, String actingUsername) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw conflict("Only paid payments can be refunded");
        }
        LocalDateTime now = LocalDateTime.now();
        Payment refunded = new Payment.Builder().copy(payment).setStatus(PaymentStatus.REFUNDED)
                .setRefundedAt(now).build();
        paymentRepository.save(refunded);
        Order order = orderRepository.findByIdForUpdate(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + payment.getOrderId()));
        orderRepository.save(copyOrder(order, OrderPaymentStatus.REFUNDED,
                payment.getPaymentId(), order.getPaidAt()));
        auditLogService.log(actingUsername, "PAYMENT_REFUNDED", paymentId,
                "Refunded payment for order " + payment.getOrderId() + ", amount R" + payment.getAmount());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", paymentId);
        response.put("status", "REFUNDED");
        response.put("refundedAt", now);
        return response;
    }

    @Transactional
    public void refundForCancelledOrder(String orderId, String actingUsername) {
        Payment payment = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID).findFirst().orElse(null);
        if (payment == null) return;
        LocalDateTime now = LocalDateTime.now();
        paymentRepository.save(new Payment.Builder().copy(payment)
                .setStatus(PaymentStatus.REFUNDED).setRefundedAt(now).build());
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        orderRepository.save(copyOrder(order, OrderPaymentStatus.REFUNDED,
                payment.getPaymentId(), order.getPaidAt()));
        auditLogService.log(actingUsername, "PAYMENT_REFUNDED", payment.getPaymentId(),
                "Automatically refunded payment for cancelled order " + orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getByOrder(String orderId, String patientId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (patientId != null && !patientId.equals(order.getPatientId())) {
            throw new AccessDeniedException("Patients may only view payments for their own orders");
        }
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getMine(String patientId) {
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public Page<Payment> search(LocalDate from, LocalDate to, PaymentStatus status,
                                int page, int size, String patientId) {
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return paymentRepository.search(patientId, status, start, end, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReceipt(String paymentId, String patientId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        if (patientId != null && !patientId.equals(payment.getPatientId())) {
            throw new AccessDeniedException("Patients may only view their own receipts");
        }
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + payment.getOrderId()));
        Patient patient = patientRepository.findById(payment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + payment.getPatientId()));
        List<Map<String, Object>> lines = new ArrayList<>();
        for (OrderItem item : orderItemRepository.findByOrderId(order.getOrderId())) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("medicine", medicationRepository.findById(item.getMedicationId())
                    .map(Medication::getMedicationName).orElse(item.getMedicationId()));
            line.put("quantity", item.getQuantity());
            line.put("unitPrice", item.getPrice());
            line.put("lineTotal", item.getSubtotal());
            lines.add(line);
        }
        BigDecimal total = payment.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = total.multiply(vatRate)
                .divide(BigDecimal.ONE.add(vatRate), 2, RoundingMode.HALF_UP);
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("receiptNumber", payment.getReceiptNumber());
        receipt.put("paymentId", paymentId);
        receipt.put("date", payment.getPaidAt());
        receipt.put("patientName", (patient.getFirstName() + " " + patient.getLastName()).trim());
        receipt.put("items", lines);
        receipt.put("subtotalExVat", total.subtract(vat).setScale(2, RoundingMode.HALF_UP));
        receipt.put("vatAmount", vat);
        receipt.put("total", total);
        receipt.put("cardBrand", payment.getCardBrand());
        receipt.put("cardLast4", payment.getCardLast4());
        receipt.put("status", payment.getStatus());
        return receipt;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> export(LocalDate from, LocalDate to) {
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();
        return paymentRepository.findForExport(start, end).stream().map(payment -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("paymentId", payment.getPaymentId());
            row.put("receipt", payment.getReceiptNumber());
            row.put("date", payment.getPaidAt());
            row.put("order", payment.getOrderId());
            row.put("patient", payment.getPatientId());
            row.put("amount", payment.getAmount());
            row.put("brand", payment.getCardBrand());
            row.put("last4", payment.getCardLast4());
            row.put("status", payment.getStatus());
            return row;
        }).toList();
    }

    private String nextReceiptNumber(LocalDateTime now) {
        LocalDate date = now.toLocalDate();
        long sequence = paymentRepository.countByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay()) + 1;
        return "RCP-" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", sequence);
    }

    private CardPaymentResponse toResponse(Payment payment) {
        return new CardPaymentResponse(payment.getPaymentId(), payment.getReceiptNumber(), payment.getOrderId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus().name(),
                payment.getCardBrand(), payment.getCardLast4(), payment.getPaidAt());
    }

    private Order copyOrder(Order order, OrderPaymentStatus status, String paymentId, LocalDateTime paidAt) {
        return new Order.Builder().setOrderId(order.getOrderId()).setPatientId(order.getPatientId())
                .setPrescriptionId(order.getPrescriptionId()).setOrderDate(order.getOrderDate())
                .setDispensingStatus(order.getDispensingStatus()).setTotalAmount(order.getTotalAmount())
                .setPharmacistId(order.getPharmacistId()).setNotes(order.getNotes())
                .setPaymentStatus(status).setPaymentId(paymentId).setPaidAt(paidAt).build();
    }

    private PaymentException conflict(String message) {
        return new PaymentException(HttpStatus.CONFLICT, null, message);
    }
}
