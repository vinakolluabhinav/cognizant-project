package com.example.deposit.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-config-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductDTO getProductById(@PathVariable Long id);
}
