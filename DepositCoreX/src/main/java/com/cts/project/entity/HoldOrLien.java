package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HoldOrLien")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HoldOrLien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HoldID")
    private Long holdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "HoldType", length = 40)
    private String holdType;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "Reason", length = 200)
    private String reason;

    @Column(name = "PlacedDate")
    private LocalDateTime placedDate;

    @Column(name = "Status", length = 30)
    private String status;
}