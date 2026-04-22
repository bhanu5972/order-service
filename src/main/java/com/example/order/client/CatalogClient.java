package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "catalog-service", url = "${services.catalog.url:http://localhost:8081}")
public interface CatalogClient {
    
    @GetMapping("/v1/products/{id}")
    ProductResponse getProduct(@PathVariable("id") UUID id);
    
    @GetMapping("/v1/products/sku/{sku}")
    ProductResponse getProductBySku(@PathVariable("sku") String sku);
    
    class ProductResponse {
        private UUID productId;
        private String sku;
        private String name;
        private BigDecimal price;
        
        public ProductResponse() {}
        
        public UUID getProductId() {
            return productId;
        }
        
        public void setProductId(UUID productId) {
            this.productId = productId;
        }
        
        public String getSku() {
            return sku;
        }
        
        public void setSku(String sku) {
            this.sku = sku;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}