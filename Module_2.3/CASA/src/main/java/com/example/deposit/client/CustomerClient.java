package com.example.deposit.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-onboarding-service")
public interface CustomerClient {

    @GetMapping("/api/v1/customer-reference/{cifNumber}")
    CustomerDTO getCustomerByCif(@PathVariable String cifNumber);
}
