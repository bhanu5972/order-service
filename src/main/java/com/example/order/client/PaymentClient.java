package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", url = "${services.payment.url:http://localhost:8084}")
public interface PaymentClient {
    
    @PostMapping("/v1/payments/charge")
    PaymentResponse charge(@RequestBody PaymentRequest request,
                           @RequestHeader("Idempotency-Key") String idempotencyKey);
    
    class PaymentRequest {
        private UUID orderId;
        private UUID customerId;
        private BigDecimal amount;
        private String method;
        
        // Getters and Setters
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        
        public UUID getCustomerId() { return customerId; }
        public void setCustomerId(UUID customerId) { this.customerId = customerId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }
    
    class PaymentResponse {
        private String paymentId;
        private Boolean success;
        private String status;
        
        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}