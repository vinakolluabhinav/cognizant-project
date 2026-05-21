package com.example.deposit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "term_deposit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "td_id")
    private Long tdId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "principal_amount", precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "rate", precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "payout_mode", length = 40)
    private String payoutMode;

    @Column(name = "status", length = 30)
    private String status;
}
