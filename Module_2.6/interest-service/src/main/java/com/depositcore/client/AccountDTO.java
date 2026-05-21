package com.depositcore.client;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountDTO {
    private Long accountId;
    private Long customerId;
    private Long productId;
    private String accountNumber;
    private String category;
    private String currency;
    private LocalDate openDate;
    private String status;
    private BigDecimal currentBalance;
}
