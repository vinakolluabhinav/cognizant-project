package com.depositcorex.Main.Client;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class AccountDTO {
    private Long accountId;
    private Long customerId;
    private Long productId;
    private String accountNumber;
    private String category;
    private String currency;
    private LocalDate openDate;
    private BigDecimal currentBalance;
    private String status;
}
