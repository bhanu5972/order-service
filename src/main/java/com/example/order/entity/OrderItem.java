package com.example.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id")
    private UUID orderItemId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(nullable = false, length = 50)
    private String sku;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    // Default constructor
    public OrderItem() {}
    
    // Constructor with fields
    public OrderItem(UUID orderId, UUID productId, String sku, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Getters and Setters
    public UUID getOrderItemId() { return orderItemId; }
    public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}