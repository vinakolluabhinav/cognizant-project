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
@Table(name = "GLPosting")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GLPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GLPostingID")
    private Long glPostingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TxnID")
    private Transaction txn;

    @Column(name = "GLAccount", length = 60)
    private String glAccount;

    @Column(name = "DebitOrCredit", length = 20)
    private String debitOrCredit;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "PostedDate")
    private LocalDateTime postedDate;
}