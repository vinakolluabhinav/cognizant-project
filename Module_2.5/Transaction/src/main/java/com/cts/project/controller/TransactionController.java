package com.cts.project.controller;

import com.cts.project.dto.TransactionRequestDTO;
import com.cts.project.dto.TransactionResponseDTO;
import com.cts.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<TransactionResponseDTO> postTransaction(@RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.postTransaction(request));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<TransactionResponseDTO> reverseTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.reverseTransaction(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<TransactionResponseDTO>> getByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<TransactionResponseDTO>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }
}
