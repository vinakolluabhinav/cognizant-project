package com.depositcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestRequestDTO {

    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    // L15 — optional product reference for rate slab lookup
    private Long productId;

    @NotNull(message = "Period start date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate periodStart;

    @NotNull(message = "Period end date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate periodEnd;

    // L3 — caller can pass explicit rate; if null, service uses product slab or default
    private BigDecimal annualRatePct;

    // L3 — caller can pass interest method; if null, defaults to SIMPLE
    private String interestMethod;
}
