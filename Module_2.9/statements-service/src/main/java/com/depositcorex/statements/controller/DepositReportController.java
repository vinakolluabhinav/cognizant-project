package com.depositcorex.statements.controller;

import com.depositcorex.statements.entity.DepositReport;
import com.depositcorex.statements.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class DepositReportController {

    private final StatementService statementService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<DepositReport> generateReport(@RequestParam String scope) {
        return ResponseEntity.ok(statementService.generateDepositReport(scope));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<DepositReport>> getAllReports() {
        return ResponseEntity.ok(statementService.getAllReports());
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<DepositReport> getReportById(@PathVariable Long reportId) {
        return ResponseEntity.ok(statementService.getReportById(reportId));
    }
}
