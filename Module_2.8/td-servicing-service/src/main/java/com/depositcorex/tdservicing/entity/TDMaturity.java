package com.depositcorex.tdservicing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "td_maturity",
    uniqueConstraints = {
        // T2 — one maturity record per TD (renewed TDs get a new TDID)
        @UniqueConstraint(name = "uk_maturity_tdid", columnNames = {"td_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TDMaturity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maturityId;

    // T2 — unique per TD
    @Column(name = "td_id", nullable = false, unique = true)
    private Long tdId;

    // Maturity amounts
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;       // original principal at maturity

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal interestAmount;        // total interest earned

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maturityAmount;        // principal + interest

    // T6 — currency
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    private LocalDate payoutDate;

    // For renewals — references the new TD created
    private Long renewedAsTdId;

    // Status: PAID / RENEWED
    @Column(nullable = false, length = 20)
    private String status;

    // L5 — transaction reference for the credit posted to CASA
    @Column
    private Long payoutTxnId;

    // T5 — audit: who triggered the maturity processing
    @Column
    private Long initiatedByUserId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
