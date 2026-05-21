package com.depositcorex.Main.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "hold_or_lien")
public class HoldOrLien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String holdType;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reason;

    @Column(nullable = false)
    private LocalDateTime placedDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status;
}
