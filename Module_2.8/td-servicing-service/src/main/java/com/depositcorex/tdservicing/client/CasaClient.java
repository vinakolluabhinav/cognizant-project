package com.depositcorex.tdservicing.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "casa-service")
public interface CasaClient {

    @GetMapping("/api/v1/accounts/term-deposit/by-tdid/{tdId}")
    TermDepositDTO getTermDepositByTdId(@PathVariable Long tdId);

    @GetMapping("/api/v1/accounts/term-deposits/maturing-today")
    java.util.List<TermDepositDTO> getTdsMaturingToday();

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);

    @PatchMapping("/api/v1/accounts/term-deposit/{tdId}/status")
    void updateTermDepositStatus(@PathVariable Long tdId, @RequestParam String status);

    @Data
    class AccountDTO {
        private Long accountId;
        private Long customerId;
        private String currency;
        private BigDecimal currentBalance;
        private String status;
    }
}
