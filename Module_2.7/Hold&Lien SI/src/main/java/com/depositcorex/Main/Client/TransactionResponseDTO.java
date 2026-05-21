package com.depositcorex.Main.Client;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long txnId;
    private Long accountId;
    private String accountNumber;
    private String txnType;
    private BigDecimal amount;
    private String narrative;
    private String channel;
    private LocalDateTime txnDate;
    private BigDecimal balanceAfter;
    private String status;
}
