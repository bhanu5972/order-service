package com.example.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Items are required")
    private List<OrderItemRequest> items;
    
    private String preferredWarehouse;
    
    // Getters and Setters
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    
    public String getPreferredWarehouse() { return preferredWarehouse; }
    public void setPreferredWarehouse(String preferredWarehouse) { this.preferredWarehouse = preferredWarehouse; }
    
    public static class OrderItemRequest {
        private String sku;
        private Integer quantity;
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}