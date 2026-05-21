package com.depositcorex.tdservicing.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "interest-service")
public interface InterestClient {

    @GetMapping("/api/v1/interest/accruals/account/{accountId}")
    List<AccrualDTO> getAccrualsByAccount(@PathVariable Long accountId);

    @Data
    class AccrualDTO {
        private Long accrualId;
        private Long accountId;
        private BigDecimal interestAmount;
        private String status;  // PENDING / POSTED
    }
}
