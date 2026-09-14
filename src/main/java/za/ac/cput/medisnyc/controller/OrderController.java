package za.ac.cput.medisnyc.controller;

/* OrderController.java
   Module 4/6: Order fulfilment endpoints.
*/

import za.ac.cput.medisnyc.domain.Order;
import za.ac.cput.medisnyc.domain.OrderStatus;
import za.ac.cput.medisnyc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Order>> getByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(orderService.getByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest request) {
        List<OrderService.ItemRequest> items = request.items.stream()
                .map(i -> new OrderService.ItemRequest(i.medicationId, i.inventoryId, i.quantity))
                .toList();
        Order created = orderService.create(request.patientId, request.prescriptionId,
                request.pharmacistId, request.notes, items);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String orderId,
                                              @RequestBody StatusUpdateRequest request,
                                              Authentication authentication) {
        String actingUsername = authentication != null ? authentication.getName() : "unknown";
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status, actingUsername));
    }
}
