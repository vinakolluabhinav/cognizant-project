package com.depositcorex.statements.repository;

import com.depositcorex.statements.entity.DepositReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositReportRepository extends JpaRepository<DepositReport, Long> {
    List<DepositReport> findByScope(String scope);
}
