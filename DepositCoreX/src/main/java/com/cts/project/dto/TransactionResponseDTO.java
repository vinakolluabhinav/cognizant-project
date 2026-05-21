package com.cts.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

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
