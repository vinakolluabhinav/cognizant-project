package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txnid")
    private Long txnId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_number", length = 40)
    private String accountNumber;

    @Column(name = "txntype", length = 40)
    private String txnType;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "narrative", length = 200)
    private String narrative;

    @Column(name = "channel", length = 40)
    private String channel;

    @Column(name = "txndate")
    private LocalDateTime txnDate;

    @Column(name = "balanceafter", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "status", length = 30)
    private String status;
}
