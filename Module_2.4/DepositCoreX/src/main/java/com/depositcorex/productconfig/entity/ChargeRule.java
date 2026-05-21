package com.depositcorex.productconfig.entity;

import com.depositcorex.productconfig.constants.ChargeMode;
import com.depositcorex.productconfig.constants.ChargeType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ChargeRule")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChargeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ChargeID;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    @JsonIgnore
    private DepositProduct product;

    @Enumerated(EnumType.STRING)
    private ChargeType chargeType;

    private Double Amount;

    @Enumerated(EnumType.STRING)
    private ChargeMode mode; // FLAT or PERCENT
}