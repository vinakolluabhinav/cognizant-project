package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Statement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StatementID")
    private Long statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "PeriodStart")
    private LocalDate periodStart;

    @Column(name = "PeriodEnd")
    private LocalDate periodEnd;

    @Column(name = "GeneratedDate")
    private LocalDateTime generatedDate;

    @Lob
    @Column(name = "SummaryJSON")
    private String summaryJson;
}