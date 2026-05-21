package com.depositcore.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/api/v1/transactions")
    TransactionResponseDTO postTransaction(@RequestBody TransactionRequestDTO request);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TransactionRequestDTO {
        private Long accountId;
        private String txnType;
        private BigDecimal amount;
        private String narrative;
        private String channel;
        private LocalDateTime txnDate;
    }

    @Data
    class TransactionResponseDTO {
        private Long txnId;
        private BigDecimal balanceAfter;
        private String status;
    }
}
