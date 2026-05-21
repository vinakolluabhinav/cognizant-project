package com.cts.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "DepositReport")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Long reportId;

    @Column(name = "Scope", length = 80)
    private String scope;

    @Lob
    @Column(name = "Metrics")
    private String metrics;

    @Column(name = "GeneratedDate")
    private LocalDateTime generatedDate;
}