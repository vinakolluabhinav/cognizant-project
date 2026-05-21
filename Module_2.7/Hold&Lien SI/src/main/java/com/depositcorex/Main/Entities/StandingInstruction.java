package com.depositcorex.Main.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "standing_instruction")
public class StandingInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long siId;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private LocalDate nextRunDate;

    @Column(nullable = false)
    private String status;
}
