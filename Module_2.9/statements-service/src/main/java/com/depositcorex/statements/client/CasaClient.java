package com.depositcorex.statements.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "casa-service")
public interface CasaClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    @GetMapping("/api/v1/accounts/active/casa")
    List<AccountDTO> getAllActiveCasaAccounts();

    @Data
    class AccountDTO {
        private Long accountId;
        private String accountNumber;
        private String category;
        private Long customerId;
        private Long productId;
        private String currency;
        private BigDecimal currentBalance;
        private String status;
        private LocalDate openDate;
    }
}
