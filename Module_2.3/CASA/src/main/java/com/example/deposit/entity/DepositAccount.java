package com.example.deposit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deposit_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "account_number", length = 40, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "category", length = 40)
    private String category;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "current_balance", precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;
}
