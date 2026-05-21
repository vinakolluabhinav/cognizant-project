package com.cts.project.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "InterestAccrual")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterestAccrual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccrualID")
    private Long accrualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "PeriodStart")
    private LocalDate periodStart;

    @Column(name = "PeriodEnd")
    private LocalDate periodEnd;

    @Column(name = "InterestAmount", precision = 19, scale = 4)
    private BigDecimal interestAmount;

    @Column(name = "CalculatedDate")
    private LocalDateTime calculatedDate;
}