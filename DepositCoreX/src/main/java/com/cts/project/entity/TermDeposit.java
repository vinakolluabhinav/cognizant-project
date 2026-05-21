package com.cts.project.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "TermDeposit")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TDID")
    private Long tdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "PrincipalAmount", precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "Rate", precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "MaturityDate")
    private LocalDate maturityDate;

    @Column(name = "PayoutMode", length = 40)
    private String payoutMode;

    @Column(name = "Status", length = 30)
    private String status;
}