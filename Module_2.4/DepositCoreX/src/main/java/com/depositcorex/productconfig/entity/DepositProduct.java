package com.depositcorex.productconfig.entity;

import java.util.List;

import com.depositcorex.productconfig.constants.Category;
import com.depositcorex.productconfig.constants.InterestMethod;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DepositProduct")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class DepositProduct {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ProductID;
    private String ProductName;
    @Enumerated(EnumType.STRING)
    private Category category;
    private Double MinAmount;
    private Double MaxAmount;
    private Integer MinTenure;
    private Integer MaxTenure;
    @Enumerated(EnumType.STRING)
    private InterestMethod interestMethod;
    private String Status; // ACTIVE, INACTIVE

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InterestTable> interestSlabs;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ChargeRule> chargeRules;
}

