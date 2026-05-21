package com.depositcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interest_posting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postingId;

    @Column(nullable = false)
    private Long accountId;

    // L1 / T1 — FK to source accrual (already present, kept)
    @Column(nullable = false)
    private Long accrualId;

    @Column(nullable = false)
    private LocalDateTime postingDate;

    // T2 — Correct monetary precision
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // T3 — Currency denomination
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PostingType postingType;

    // L7 — GL linkage: reference to the transaction created in Transaction service
    // Null until GL posting is confirmed
    @Column
    private Long glTransactionId;
}
