package com.depositcorex.Main.Dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HoldRequest {
    private Long accountId;
    private BigDecimal amount;
    private String reason;
    private String type; // HOLD or LIEN
}