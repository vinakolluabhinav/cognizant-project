package com.depositcorex.Main.Dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private Long accountId;
    private BigDecimal availableBalance;
}