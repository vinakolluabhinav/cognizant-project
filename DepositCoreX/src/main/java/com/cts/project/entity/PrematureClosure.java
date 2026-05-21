package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PrematureClosure")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrematureClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClosureID")
    private Long closureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TDID")
    private TermDeposit td;

    @Column(name = "ClosureDate")
    private LocalDate closureDate;

    @Column(name = "PenalRate", precision = 9, scale = 6)
    private BigDecimal penalRate;

    @Column(name = "CalculatedAmount", precision = 19, scale = 4)
    private BigDecimal calculatedAmount;

    @Column(name = "Status", length = 30)
    private String status;
}