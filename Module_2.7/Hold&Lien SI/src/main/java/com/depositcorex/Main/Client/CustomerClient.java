package com.depositcorex.Main.Client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-onboarding-service")
public interface CustomerClient {

    @GetMapping("/api/v1/customer-reference/by-customerid/{customerId}")
    CustomerDTO getByCustomerId(@PathVariable Long customerId);

    @Data
    class CustomerDTO {
        private Long customerID;
        private Long userId;
        private String cifNumber;
        private String fullName;
    }
}
