package com.cts.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.project.dto.TransactionRequestDTO;
import com.cts.project.dto.TransactionResponseDTO;
import com.cts.project.entity.DepositAccount;
import com.cts.project.entity.GLPosting;
import com.cts.project.entity.Transaction;
import com.cts.project.repository.DepositAccountRepository;
import com.cts.project.repository.GLPostingRepository;
import com.cts.project.repository.TransactionRepository;
import com.cts.project.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private GLPostingRepository glPostingRepository;

    @Override
    @Transactional
    public TransactionResponseDTO postTransaction(TransactionRequestDTO request) {
        DepositAccount account = depositAccountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountId()));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new RuntimeException("Account is not active. Current status: " + account.getStatus());
        }

        // Overdraft check for DEBIT and FEE
        if (("DEBIT".equalsIgnoreCase(request.getTxnType()) || "FEE".equalsIgnoreCase(request.getTxnType()))
                && request.getAmount() != null
                && getLatestBalance(account).subtract(request.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient balance for account: " + account.getAccountNumber());
        }

        BigDecimal balanceAfter = calculateBalanceAfter(account, request.getTxnType(), request.getAmount());

        Transaction txn = Transaction.builder()
                .account(account)
                .txnType(request.getTxnType())
                .amount(request.getAmount())
                .narrative(request.getNarrative())
                .channel(request.getChannel())
                .txnDate(request.getTxnDate() != null ? request.getTxnDate() : LocalDateTime.now())
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .build();

        txn = transactionRepository.save(txn);

        // Auto-generate GL Posting
        postGL(txn, request.getTxnType());

        return mapToResponse(txn);
    }

    @Override
    @Transactional
    public TransactionResponseDTO reverseTransaction(Long txnId) {
        Transaction original = transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txnId));

        if ("REVERSED".equalsIgnoreCase(original.getStatus())) {
            throw new RuntimeException("Transaction already reversed: " + txnId);
        }

        // Mark original as reversed
        original.setStatus("REVERSED");
        transactionRepository.save(original);

        // Determine reversal type
        String reversalType = "CREDIT".equalsIgnoreCase(original.getTxnType()) ? "DEBIT" : "CREDIT";

        BigDecimal balanceAfter = calculateBalanceAfter(original.getAccount(), reversalType, original.getAmount());

        Transaction reversal = Transaction.builder()
                .account(original.getAccount())
                .txnType("REVERSAL")
                .amount(original.getAmount())
                .narrative("Reversal of TxnID: " + txnId)
                .channel(original.getChannel())
                .txnDate(LocalDateTime.now())
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .build();

        reversal = transactionRepository.save(reversal);

        // Auto-generate counter GL Posting
        postGL(reversal, reversalType);

        return mapToResponse(reversal);
    }

    @Override
    public TransactionResponseDTO getById(Long txnId) {
        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txnId));
        return mapToResponse(txn);
    }

    @Override
    public List<TransactionResponseDTO> getByAccountId(Long accountId) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getAccount() != null && t.getAccount().getAccountId().equals(accountId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponseDTO> getAll() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    private BigDecimal getLatestBalance(DepositAccount account) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getAccount() != null && t.getAccount().getAccountId().equals(account.getAccountId()))
                .filter(t -> "POSTED".equalsIgnoreCase(t.getStatus()))
                .reduce((first, second) -> second)
                .map(Transaction::getBalanceAfter)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateBalanceAfter(DepositAccount account, String txnType, BigDecimal amount) {
        BigDecimal current = getLatestBalance(account);
        if ("CREDIT".equalsIgnoreCase(txnType)) {
            return current.add(amount);
        } else {
            return current.subtract(amount);
        }
    }

    private void postGL(Transaction txn, String txnType) {
        String glAccount = "CREDIT".equalsIgnoreCase(txnType)
                ? "GL-DEPOSITS-CREDIT"
                : "GL-DEPOSITS-DEBIT";

        GLPosting gl = GLPosting.builder()
                .txn(txn)
                .glAccount(glAccount)
                .debitOrCredit(txnType.toUpperCase())
                .amount(txn.getAmount())
                .postedDate(LocalDateTime.now())
                .build();

        glPostingRepository.save(gl);
    }

    private TransactionResponseDTO mapToResponse(Transaction txn) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTxnId(txn.getTxnId());
        dto.setAccountId(txn.getAccount() != null ? txn.getAccount().getAccountId() : null);
        dto.setAccountNumber(txn.getAccount() != null ? txn.getAccount().getAccountNumber() : null);
        dto.setTxnType(txn.getTxnType());
        dto.setAmount(txn.getAmount());
        dto.setNarrative(txn.getNarrative());
        dto.setChannel(txn.getChannel());
        dto.setTxnDate(txn.getTxnDate());
        dto.setBalanceAfter(txn.getBalanceAfter());
        dto.setStatus(txn.getStatus());
        return dto;
    }
}
