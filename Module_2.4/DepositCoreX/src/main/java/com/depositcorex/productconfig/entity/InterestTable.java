package com.depositcorex.productconfig.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
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
@Table(name = "InterestTable")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class InterestTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long TableID;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    @JsonIgnore // Prevents infinite loops when converting to JSON
    private DepositProduct product;

    private Integer TenureFrom;
    private Integer TenureTo;
    private Double Rate;
    private LocalDate EffectiveFrom;
    private LocalDate EffectiveTo;
}