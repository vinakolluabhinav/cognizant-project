package com.depositcorex.Main.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/api/v1/transactions")
    TransactionResponseDTO postTransaction(@RequestBody TransactionRequestDTO request);
}
