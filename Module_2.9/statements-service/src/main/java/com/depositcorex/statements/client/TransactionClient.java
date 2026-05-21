package com.depositcorex.statements.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @GetMapping("/api/v1/transactions/account/{accountId}")
    List<TransactionDTO> getTransactionsByAccount(@PathVariable Long accountId);
}
