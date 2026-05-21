package com.depositcorex.statements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_report")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false, length = 100)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String metrics;

    @CreationTimestamp
    private LocalDateTime generatedDate;
}
