package com.cts.project.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "casa-service")
public interface CasaAccountClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    @PutMapping("/api/v1/accounts/{accountId}/balance")
    void updateBalance(@PathVariable Long accountId, @RequestParam BigDecimal balance);
}
