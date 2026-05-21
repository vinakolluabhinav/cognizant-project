package com.cts.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.project.dto.TransactionRequestDTO;
import com.cts.project.dto.TransactionResponseDTO;
import com.cts.project.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Post a new debit/credit/fee transaction
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> postTransaction(@RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.postTransaction(request));
    }

    // Reverse an existing transaction
    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransactionResponseDTO> reverseTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.reverseTransaction(id));
    }

    // Get transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    // Get all transactions for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> getByAccountId(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }
}
