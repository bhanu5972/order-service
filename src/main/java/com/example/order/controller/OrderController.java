package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Order Management", description = "Endpoints for managing orders")
public class OrderController {
    
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates an order with inventory reservation and payment")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /v1/orders - idempotencyKey: {}", idempotencyKey);
        OrderResponse response = orderService.createOrder(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all orders", description = "Returns all orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("GET /v1/orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Returns a single order")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        log.info("GET /v1/orders/{}", orderId);
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
    
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancels an order and releases inventory")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        log.info("POST /v1/orders/{}/cancel", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}