package com.cts.project.entity;

import java.math.BigDecimal;

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
@Table(name = "ChargeRule")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChargeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChargeID")
    private Long chargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID")
    private DepositProduct product;

    @Column(name = "ChargeType", length = 80)
    private String chargeType;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "Mode", length = 40)
    private String mode;
}
