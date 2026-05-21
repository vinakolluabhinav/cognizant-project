package com.depositcorex.Main.Dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SIRequest {
    private Long fromAccId;
    private Long toAccId;
    private BigDecimal amount;
    private String frequency;
}