package com.cts.project.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DepositProduct")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Long productId;

    @Column(name = "ProductName", length = 150, nullable = false, unique = true)
    private String productName;

    @Column(name = "Category", length = 80)
    private String category;

    @Column(name = "MinAmount", precision = 19, scale = 4)
    private BigDecimal minAmount;

    @Column(name = "MaxAmount", precision = 19, scale = 4)
    private BigDecimal maxAmount;

    @Column(name = "MinTenure")
    private Integer minTenure;

    @Column(name = "MaxTenure")
    private Integer maxTenure;

    @Column(name = "InterestMethod", length = 50)
    private String interestMethod;

    @Column(name = "Status", length = 30)
    private String status;
}
