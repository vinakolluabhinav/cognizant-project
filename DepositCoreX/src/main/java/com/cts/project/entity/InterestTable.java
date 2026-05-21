package com.cts.project.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "InterestTable")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterestTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TableID")
    private Long tableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID")
    private DepositProduct product;

    @Column(name = "TenureFrom")
    private Integer tenureFrom;

    @Column(name = "TenureTo")
    private Integer tenureTo;

    @Column(name = "Rate", precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(name = "EffectiveFrom")
    private LocalDate effectiveFrom;

    @Column(name = "EffectiveTo")
    private LocalDate effectiveTo;
}