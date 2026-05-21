package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TDMaturity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TDMaturity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaturityID")
    private Long maturityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TDID")
    private TermDeposit td;

    @Column(name = "MaturityAmount", precision = 19, scale = 4)
    private BigDecimal maturityAmount;

    @Column(name = "PayoutDate")
    private LocalDate payoutDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RenewedAsTDID")
    private TermDeposit renewedAsTd;

    @Column(name = "Status", length = 30)
    private String status;
}
