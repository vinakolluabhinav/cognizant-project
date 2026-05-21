package com.example.deposit.controller;

import com.example.deposit.dto.AccountResponse;
import com.example.deposit.dto.CasaAccountRequest;
import com.example.deposit.dto.TermDepositRequest;
import com.example.deposit.entity.DepositAccount;
import com.example.deposit.entity.TermDeposit;
import com.example.deposit.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/casa")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<AccountResponse> createCasaAccount(@RequestBody CasaAccountRequest request) {
        return ResponseEntity.ok(accountService.createCasaAccount(request));
    }

    @PostMapping("/term-deposit")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<AccountResponse> createTermDepositAccount(@RequestBody TermDepositRequest request) {
        return ResponseEntity.ok(accountService.createTermDepositAccount(request));
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<DepositAccount> getDepositAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getDepositAccountById(accountId));
    }

    @GetMapping("/term-deposit/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<TermDeposit> getTermDepositByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getTermDepositByAccountId(accountId));
    }

    @GetMapping("/term-deposits/maturing-today")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<List<TermDeposit>> getTdsMaturingToday() {
        return ResponseEntity.ok(accountService.getTdsMaturingToday());
    }

    @GetMapping("/term-deposit/by-tdid/{tdId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<TermDeposit> getTermDepositByTdId(@PathVariable Long tdId) {
        return ResponseEntity.ok(accountService.getTermDepositByTdId(tdId));
    }

    /**
     * Internal endpoint: called by Transaction service via Feign to reflect balance after a posting.
     * Not exposed for direct customer use — requires at minimum BRANCH_OFFICER role propagated via Feign.
     */
    @GetMapping("/active/casa")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<DepositAccount>> getAllActiveCasaAccounts() {
        return ResponseEntity.ok(accountService.getAllActiveCasaAccounts());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<DepositAccount>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @PutMapping("/{accountId}/balance")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<Void> updateBalance(
            @PathVariable Long accountId,
            @RequestParam BigDecimal balance) {
        accountService.updateBalance(accountId, balance);
        return ResponseEntity.ok().build();
    }

    /**
     * Internal endpoint: called by TD Servicing via Feign after maturity or premature closure.
     */
    @PatchMapping("/term-deposit/{tdId}/status")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<Void> updateTermDepositStatus(
            @PathVariable Long tdId,
            @RequestParam String status) {
        accountService.updateTermDepositStatus(tdId, status);
        return ResponseEntity.ok().build();
    }
}
