package com.example.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "order_status", nullable = false)
    private String orderStatus;  // PENDING, CONFIRMED, CANCELLED, DELIVERED
    
    @Column(name = "payment_status")
    private String paymentStatus;  // PENDING, PAID, FAILED, REFUNDED
    
    @Column(name = "order_total", precision = 10, scale = 2)
    private BigDecimal orderTotal;
    
    @Column(name = "reservation_id")
    private String reservationId;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderStatus == null) orderStatus = "PENDING";
        if (paymentStatus == null) paymentStatus = "PENDING";
    }
    
    // Default constructor
    public Order() {}
    
    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public BigDecimal getOrderTotal() { return orderTotal; }
    public void setOrderTotal(BigDecimal orderTotal) { this.orderTotal = orderTotal; }
    
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}