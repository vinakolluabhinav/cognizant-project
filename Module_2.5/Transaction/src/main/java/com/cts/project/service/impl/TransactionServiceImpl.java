package com.cts.project.service.impl;

import com.cts.project.client.AccountDTO;
import com.cts.project.client.CasaAccountClient;
import com.cts.project.client.CustomerClient;
import com.cts.project.client.NotificationClient;
import com.cts.project.dto.TransactionRequestDTO;
import com.cts.project.dto.TransactionResponseDTO;
import com.cts.project.entity.GLPosting;
import com.cts.project.entity.Transaction;
import com.cts.project.repository.GLPostingRepository;
import com.cts.project.repository.TransactionRepository;
import com.cts.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final GLPostingRepository   glPostingRepository;
    private final CasaAccountClient     casaAccountClient;
    private final CustomerClient        customerClient;
    private final NotificationClient    notificationClient;

    @Override
    @Transactional
    public TransactionResponseDTO postTransaction(TransactionRequestDTO request) {
        AccountDTO account = casaAccountClient.getAccountById(request.getAccountId());

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new RuntimeException("Account is not active: " + account.getAccountNumber());
        }

        if (("DEBIT".equalsIgnoreCase(request.getTxnType()) || "FEE".equalsIgnoreCase(request.getTxnType()))
                && getLatestBalance(request.getAccountId()).subtract(request.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient balance for account: " + account.getAccountNumber());
        }

        BigDecimal balanceAfter = calculateBalanceAfter(request.getAccountId(), request.getTxnType(), request.getAmount());

        Transaction txn = Transaction.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .txnType(request.getTxnType())
                .amount(request.getAmount())
                .narrative(request.getNarrative())
                .channel(request.getChannel())
                .txnDate(request.getTxnDate() != null ? request.getTxnDate() : LocalDateTime.now())
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .build();

        txn = transactionRepository.save(txn);
        casaAccountClient.updateBalance(account.getAccountId(), balanceAfter);
        postGL(txn, request.getTxnType());

        // Send notification to account holder
        sendTransactionNotification(account, txn, balanceAfter);

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

        original.setStatus("REVERSED");
        transactionRepository.save(original);

        String reversalType = "CREDIT".equalsIgnoreCase(original.getTxnType()) ? "DEBIT" : "CREDIT";
        BigDecimal balanceAfter = calculateBalanceAfter(original.getAccountId(), reversalType, original.getAmount());

        Transaction reversal = Transaction.builder()
                .accountId(original.getAccountId())
                .accountNumber(original.getAccountNumber())
                .txnType("REVERSAL")
                .amount(original.getAmount())
                .narrative("Reversal of TxnID: " + txnId)
                .channel(original.getChannel())
                .txnDate(LocalDateTime.now())
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .build();

        reversal = transactionRepository.save(reversal);
        casaAccountClient.updateBalance(reversal.getAccountId(), balanceAfter);
        postGL(reversal, reversalType);
        return mapToResponse(reversal);
    }

    @Override
    public TransactionResponseDTO getById(Long txnId) {
        return mapToResponse(transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txnId)));
    }

    @Override
    public List<TransactionResponseDTO> getByAccountId(Long accountId) {
        return transactionRepository.findByAccountIdOrderByTxnDateDesc(accountId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponseDTO> getAll() {
        return transactionRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void sendTransactionNotification(AccountDTO account, Transaction txn, BigDecimal balanceAfter) {
        try {
            if (account.getCustomerId() == null) return;
            CustomerClient.CustomerDTO customer =
                    customerClient.getByCustomerId(account.getCustomerId());
            if (customer.getUserId() == null) return;

            String sign    = "CREDIT".equalsIgnoreCase(txn.getTxnType()) ? "+" : "-";
            String message = String.format("%s %s%.2f on account %s. Balance: %.2f. Ref: %s",
                    "CREDIT".equalsIgnoreCase(txn.getTxnType()) ? "Amount credited" : "Amount debited",
                    sign, txn.getAmount(),
                    account.getAccountNumber(),
                    balanceAfter,
                    txn.getNarrative());

            notificationClient.sendNotification(
                    new NotificationClient.NotificationRequest(
                            customer.getUserId(), message, "Balance"));
        } catch (Exception e) {
            // Never fail the transaction because of a notification error
            log.warn("Failed to send transaction notification for account {}: {}",
                    account.getAccountId(), e.getMessage());
        }
    }

    private BigDecimal getLatestBalance(Long accountId) {
        // Always read live balance from CASA — single source of truth
        // Never trust transaction table balanceAfter for current balance
        AccountDTO account = casaAccountClient.getAccountById(accountId);
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }

    private BigDecimal calculateBalanceAfter(Long accountId, String txnType, BigDecimal amount) {
        BigDecimal current = getLatestBalance(accountId);
        return "CREDIT".equalsIgnoreCase(txnType) ? current.add(amount) : current.subtract(amount);
    }

    private void postGL(Transaction txn, String txnType) {
        String glAccount = "CREDIT".equalsIgnoreCase(txnType) ? "GL-DEPOSITS-CREDIT" : "GL-DEPOSITS-DEBIT";
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
        dto.setAccountId(txn.getAccountId());
        dto.setAccountNumber(txn.getAccountNumber());
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
