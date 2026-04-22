package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "inventory-service", url = "${services.inventory.url:http://localhost:8082}")
public interface InventoryClient {
    
    @PostMapping("/v1/inventory/reserve")
    ReserveResponse reserve(@RequestBody ReserveRequest request);
    
    @PostMapping("/v1/inventory/release/{reservationId}")
    void release(@RequestParam("reservationId") String reservationId);
    
    class ReserveRequest {
        private UUID orderId;
        private List<ItemReservation> items;
        private String preferredWarehouse;
        
        // Getters and Setters
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        
        public List<ItemReservation> getItems() { return items; }
        public void setItems(List<ItemReservation> items) { this.items = items; }
        
        public String getPreferredWarehouse() { return preferredWarehouse; }
        public void setPreferredWarehouse(String preferredWarehouse) { this.preferredWarehouse = preferredWarehouse; }
    }
    
    class ItemReservation {
        private UUID productId;
        private Integer quantity;
        private String warehouse;
        
        // Getters and Setters
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getWarehouse() { return warehouse; }
        public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    }
    
    class ReserveResponse {
        private String reservationId;
        private Boolean success;
        
        // Getters and Setters
        public String getReservationId() { return reservationId; }
        public void setReservationId(String reservationId) { this.reservationId = reservationId; }
        
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
    }
}