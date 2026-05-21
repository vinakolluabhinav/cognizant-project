package com.depositcorex.statements.controller;

import com.depositcorex.statements.entity.DepositReport;
import com.depositcorex.statements.entity.Statement;
import com.depositcorex.statements.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<?> generateStatement(
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            Authentication auth) {
        try {
            Long userId = parseLong(auth.getName()); // X-User-Id set as principal by GatewayAuthFilter
            String role = extractRole(auth);
            return ResponseEntity.ok(
                statementService.generateStatement(accountId, periodStart, periodEnd, userId, role));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<Statement>> getStatements(@PathVariable Long accountId) {
        return ResponseEntity.ok(statementService.getStatements(accountId));
    }

    @GetMapping("/{statementId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<Statement> getStatementById(@PathVariable Long statementId) {
        return ResponseEntity.ok(statementService.getStatementById(statementId));
    }

    // ── Deposit Reports ──

    @PostMapping("/reports/generate")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<DepositReport> generateReport(@RequestParam String scope) {
        return ResponseEntity.ok(statementService.generateDepositReport(scope));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<DepositReport>> getAllReports() {
        return ResponseEntity.ok(statementService.getAllReports());
    }

    @GetMapping("/reports/{reportId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<DepositReport> getReportById(@PathVariable Long reportId) {
        return ResponseEntity.ok(statementService.getReportById(reportId));
    }

    // ── Helpers ──

    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .findFirst()
                .orElse("");
    }

    private Long parseLong(String val) {
        try { return Long.parseLong(val); }
        catch (NumberFormatException e) { return null; }
    }
}
