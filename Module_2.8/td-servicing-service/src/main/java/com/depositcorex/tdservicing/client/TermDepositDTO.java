package com.depositcorex.tdservicing.client;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TermDepositDTO {
    private Long tdId;
    private Long accountId;
    private BigDecimal principalAmount;
    private Integer tenureMonths;
    private BigDecimal rate;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private String payoutMode;
    private String status;
    private String currency;   // T6
    private Long customerId;   // for notifications (L15)
}
