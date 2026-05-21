package com.depositcorex.tdservicing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "premature_closure",
    uniqueConstraints = {
        // T3 — A TD can only be prematurely closed once
        @UniqueConstraint(name = "uk_closure_tdid", columnNames = {"td_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrematureClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long closureId;

    // T3 — unique per TD
    @Column(name = "td_id", nullable = false, unique = true)
    private Long tdId;

    @Column(nullable = false)
    private LocalDate closureDate;

    // L3 — itemized payout breakdown
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;       // original principal

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal earnedInterest;        // interest calculated at effective rate

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal penalDeduction;        // interest lost due to penalty

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal netPayout;             // T4 — renamed from calculatedAmount → netPayout

    // L4 — store both rates at closure time
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal originalRate;          // rate from TD at closure time

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal penalRate;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal effectiveRate;         // originalRate − penalRate

    // T6 — currency
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    // L8 — defined status values: CLOSED / FAILED
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "CLOSED";

    // L5 — transaction reference for the payout
    @Column
    private Long payoutTxnId;

    // T5 — audit: who initiated
    @Column
    private Long initiatedByUserId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
