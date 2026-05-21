package com.depositcorex.statements.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "statement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @CreationTimestamp
    private LocalDateTime generatedDate;

    @Column(columnDefinition = "TEXT")
    private String summaryJson;
}
