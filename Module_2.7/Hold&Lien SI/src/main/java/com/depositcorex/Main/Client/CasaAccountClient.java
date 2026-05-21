package com.depositcorex.Main.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "casa-service")
public interface CasaAccountClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable Long accountId);
}
