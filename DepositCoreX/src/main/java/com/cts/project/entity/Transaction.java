package com.cts.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "Transaction")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TxnID")
    private Long txnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private DepositAccount account;

    @Column(name = "TxnType", length = 40)
    private String txnType;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "Narrative", length = 200)
    private String narrative;

    @Column(name = "Channel", length = 40)
    private String channel;

    @Column(name = "TxnDate")
    private LocalDateTime txnDate;

    @Column(name = "BalanceAfter", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "Status", length = 30)
    private String status;
}