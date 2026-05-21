package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "StandingInstruction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StandingInstruction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SIID")
    private Long siId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FromAccountID")
    private DepositAccount fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ToAccountID")
    private DepositAccount toAccount;

    @Column(name = "Frequency", length = 40)
    private String frequency;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "NextRunDate")
    private LocalDate nextRunDate;

    @Column(name = "Status", length = 30)
    private String status;
}