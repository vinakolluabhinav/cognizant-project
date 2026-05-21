package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "InterestPosting")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterestPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PostingID")
    private Long postingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "PostingDate")
    private LocalDateTime postingDate;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "PostingType", length = 80)
    private String postingType;
}
