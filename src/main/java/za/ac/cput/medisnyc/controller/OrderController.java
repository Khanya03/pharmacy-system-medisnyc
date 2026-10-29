package za.ac.cput.medisnyc.controller;

/* OrderController.java
   Module 4/6: Order fulfilment endpoints.
*/

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.domain.User;
import za.ac.cput.medisnyc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public static class OrderItemRequest {
        public String medicationId;
        public String inventoryId;
        public int quantity;
    }

    public static class CreateOrderRequest {
        public String patientId;
        public String prescriptionId;
        public String pharmacistId;
        public String notes;
        public List<OrderItemRequest> items;
    }

    public static class StatusUpdateRequest {
        public OrderStatus status;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<List<Order>> getMine(@AuthenticationPrincipal User user) {
        if (user.getLinkedProfileId() == null) {
            throw new AccessDeniedException("Patient account has no linked patient record");
        }
        return ResponseEntity.ok(orderService.getByPatient(user.getLinkedProfileId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getById(@PathVariable String orderId,
                                         @AuthenticationPrincipal User user,
                                         Authentication authentication) {
        Order order = orderService.getById(orderId);
        if (hasRole(authentication, "PATIENT") && !order.getPatientId().equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Patients may only view their own orders");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Order>> getByPatient(@PathVariable String patientId,
                                                    @AuthenticationPrincipal User user,
                                                    Authentication authentication) {
        if (hasRole(authentication, "PATIENT") && !patientId.equals(user.getLinkedProfileId())) {
            throw new AccessDeniedException("Patients may only view their own orders");
        }
        return ResponseEntity.ok(orderService.getByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest request,
                                        @AuthenticationPrincipal User user,
                                        Authentication authentication) {
        if (hasRole(authentication, "PATIENT")) {
            if (user.getLinkedProfileId() == null) {
                throw new AccessDeniedException("Patient account has no linked patient record");
            }
            request.patientId = user.getLinkedProfileId();
        }
        if (request.items == null) {
            throw new IllegalArgumentException("An order needs at least one item");
        }
        List<OrderService.ItemRequest> items = request.items.stream()
                .map(i -> new OrderService.ItemRequest(i.medicationId, i.inventoryId, i.quantity))
                .toList();
        Order created = orderService.create(request.patientId, request.prescriptionId,
                hasRole(authentication, "PATIENT") ? user.getUsername() : authentication.getName(),
                request.notes, items);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String orderId,
                                              @RequestBody StatusUpdateRequest request,
                                              Authentication authentication) {
        String actingUsername = authentication != null ? authentication.getName() : "unknown";
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status, actingUsername));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
