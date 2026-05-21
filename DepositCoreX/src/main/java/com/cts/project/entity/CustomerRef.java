package com.cts.project.entity;

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
@Table(name = "CustomerRef")
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long customerId;

    @Column(name = "CIFNumber", length = 50, unique = true)
    private String cifNumber;

    @Column(name = "FullName", length = 200)
    private String fullName;

    @Column(name = "Segment", length = 100)
    private String segment;

    @Column(name = "KYCStatus", length = 50)
    private String kycStatus;

    @Column(name = "Status", length = 30)
    private String status;
}