package com.cts.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GLPostingResponseDTO {
    private Long glPostingId;
    private Long txnId;
    private String glAccount;
    private String debitOrCredit;
    private BigDecimal amount;
    private LocalDateTime postedDate;
}
