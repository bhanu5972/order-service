package com.example.order.service;

import com.example.order.client.CatalogClient;
import com.example.order.client.InventoryClient;
import com.example.order.client.PaymentClient;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.repository.OrderItemRepository;
import com.example.order.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    
    // Metrics
    private final Counter ordersPlacedCounter;
    private final Counter ordersFailedCounter;
    
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CatalogClient catalogClient,
                        InventoryClient inventoryClient,
                        PaymentClient paymentClient,
                        MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        
        this.ordersPlacedCounter = Counter.builder("orders_placed_total")
                .description("Total number of orders placed successfully")
                .register(meterRegistry);
        
        this.ordersFailedCounter = Counter.builder("orders_failed_total")
                .description("Total number of failed orders")
                .register(meterRegistry);
    }
    
    @Transactional
    public OrderResponse createOrder(String idempotencyKey, CreateOrderRequest request) {
        log.info("Creating order with idempotency key: {}", idempotencyKey);
        
        // Check idempotency
        Order existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingOrder != null) {
            log.info("Returning existing order for idempotency key: {}", idempotencyKey);
            return mapToResponse(existingOrder);
        }
        
        // Step 1: Get product prices from Catalog Service
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            CatalogClient.ProductResponse product = catalogClient.getProductBySku(item.getSku());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
            
            OrderItem orderItem = new OrderItem(
                null, // orderId will be set after order is saved
                product.getProductId(),
                product.getSku(),
                item.getQuantity(),
                product.getPrice()
            );
            orderItems.add(orderItem);
        }
        
        // Step 2: Calculate total (subtotal + 5% tax + $10 shipping)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal shipping = BigDecimal.valueOf(10.00);
        BigDecimal orderTotal = subtotal.add(tax).add(shipping);
        
        // Step 3: Create order (PENDING status)
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setOrderTotal(orderTotal);
        order.setIdempotencyKey(idempotencyKey);
        order = orderRepository.save(order);
        
        // Set orderId for items and save
        final UUID orderId = order.getOrderId();
        orderItems.forEach(item -> item.setOrderId(orderId));
        orderItemRepository.saveAll(orderItems);
        
        // Step 4: Reserve inventory
        InventoryClient.ReserveRequest reserveRequest = new InventoryClient.ReserveRequest();
        reserveRequest.setOrderId(orderId);
        reserveRequest.setPreferredWarehouse(request.getPreferredWarehouse());
        
        List<InventoryClient.ItemReservation> items = orderItems.stream().map(item -> {
            InventoryClient.ItemReservation reservation = new InventoryClient.ItemReservation();
            reservation.setProductId(item.getProductId());
            reservation.setQuantity(item.getQuantity());
            return reservation;
        }).collect(Collectors.toList());
        reserveRequest.setItems(items);
        
        InventoryClient.ReserveResponse reserveResponse;
        try {
            reserveResponse = inventoryClient.reserve(reserveRequest);
            if (!reserveResponse.getSuccess()) {
                throw new RuntimeException("Inventory reservation failed");
            }
            order.setReservationId(reserveResponse.getReservationId());
        } catch (Exception e) {
            log.error("Inventory reservation failed", e);
            order.setOrderStatus("CANCELLED");
            orderRepository.save(order);
            ordersFailedCounter.increment();
            throw new RuntimeException("Failed to reserve inventory: " + e.getMessage());
        }
        
        // Step 5: Process payment
        PaymentClient.PaymentRequest paymentRequest = new PaymentClient.PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setCustomerId(request.getCustomerId());
        paymentRequest.setAmount(orderTotal);
        paymentRequest.setMethod("CREDIT_CARD");
        
        PaymentClient.PaymentResponse paymentResponse;
        try {
            paymentResponse = paymentClient.charge(paymentRequest, idempotencyKey + "-payment");
            if (!paymentResponse.getSuccess()) {
                throw new RuntimeException("Payment failed");
            }
            order.setPaymentStatus("PAID");
        } catch (Exception e) {
            log.error("Payment failed, releasing inventory", e);
            // Release inventory on payment failure
            inventoryClient.release(order.getReservationId());
            order.setOrderStatus("CANCELLED");
            order.setPaymentStatus("FAILED");
            orderRepository.save(order);
            ordersFailedCounter.increment();
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }
        
        // Step 6: Confirm order
        order.setOrderStatus("CONFIRMED");
        orderRepository.save(order);
        
        ordersPlacedCounter.increment();
        log.info("Order created successfully: {}", order.getOrderId());
        
        return mapToResponse(order);
    }
    
    @Transactional
    public void cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if ("CONFIRMED".equals(order.getOrderStatus()) || "PENDING".equals(order.getOrderStatus())) {
            // Release inventory
            if (order.getReservationId() != null) {
                inventoryClient.release(order.getReservationId());
            }
            order.setOrderStatus("CANCELLED");
            orderRepository.save(order);
            log.info("Order cancelled: {}", orderId);
        } else {
            throw new RuntimeException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }
    }
    
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private OrderResponse mapToResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
        
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderTotal(order.getOrderTotal());
        response.setCreatedAt(order.getCreatedAt());
        
        List<OrderResponse.OrderItemResponse> itemResponses = items.stream().map(item -> {
            OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
            itemResponse.setSku(item.getSku());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            return itemResponse;
        }).collect(Collectors.toList());
        response.setItems(itemResponses);
        
        return response;
    }
}