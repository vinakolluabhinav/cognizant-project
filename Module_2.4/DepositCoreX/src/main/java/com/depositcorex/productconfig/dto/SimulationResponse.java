package com.depositcorex.productconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimulationResponse {
    private Double principal;
    private Double rateUsed;
    private Double interestAmount;
    private Double maturityAmount;
    private String method;
}