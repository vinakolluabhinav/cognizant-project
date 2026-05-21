package com.depositcorex.tdservicing.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private Long accountId;
    private String txnType;
    private BigDecimal amount;
    private String narrative;
    private String channel;
    private LocalDateTime txnDate;
}
