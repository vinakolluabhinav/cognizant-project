package com.depositcore.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "casa-service")
public interface CasaAccountClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    // Fetch all active CASA accounts for scheduler enrollment
    @GetMapping("/api/v1/accounts/active/casa")
    List<AccountDTO> getAllActiveCasaAccounts();

    @PutMapping("/api/v1/accounts/{accountId}/balance")
    void updateBalance(@PathVariable Long accountId, @RequestParam BigDecimal balance);
}
