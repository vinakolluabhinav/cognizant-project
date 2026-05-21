package com.depositcorex.statements.repository;

import com.depositcorex.statements.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatementRepository extends JpaRepository<Statement, Long> {
    List<Statement> findByAccountIdOrderByGeneratedDateDesc(Long accountId);
    // Bug 5 — duplicate check
    Optional<Statement> findByAccountIdAndPeriodStartAndPeriodEnd(
            Long accountId, LocalDate periodStart, LocalDate periodEnd);
}
