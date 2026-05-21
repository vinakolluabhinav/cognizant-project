package com.depositcorex.statements.service;

import com.depositcorex.statements.client.CasaClient;
import com.depositcorex.statements.client.CustomerClient;
import com.depositcorex.statements.client.TransactionClient;
import com.depositcorex.statements.client.TransactionDTO;
import com.depositcorex.statements.entity.DepositReport;
import com.depositcorex.statements.entity.Statement;
import com.depositcorex.statements.exception.ResourceNotFoundException;
import com.depositcorex.statements.repository.DepositReportRepository;
import com.depositcorex.statements.repository.StatementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final StatementRepository statementRepository;
    private final DepositReportRepository depositReportRepository;
    private final TransactionClient transactionClient;
    private final CasaClient casaClient;
    private final CustomerClient customerClient;
    private final ObjectMapper objectMapper;

    public Statement generateStatement(Long accountId, LocalDate periodStart, LocalDate periodEnd,
                                       Long requestingUserId, String requestingRole) {

        // Validate account exists
        CasaClient.AccountDTO account = casaClient.getAccountById(accountId);

        // Bug 4 — Ownership check: CUSTOMER can only access their own accounts
        if ("CUSTOMER".equalsIgnoreCase(requestingRole)) {
            if (account.getCustomerId() == null) {
                throw new SecurityException("Account has no linked customer");
            }
            // Resolve customerId → userId via customer-onboarding service
            try {
                CustomerClient.CustomerDTO customer =
                        customerClient.getByCustomerId(account.getCustomerId());
                if (customer.getUserId() == null ||
                    !customer.getUserId().equals(requestingUserId)) {
                    throw new SecurityException(
                        "Access denied: account " + accountId + " does not belong to you");
                }
            } catch (SecurityException e) {
                throw e; // re-throw security exceptions
            } catch (Exception e) {
                throw new SecurityException(
                    "Access denied: could not verify account ownership");
            }
        }

        // Bug 5 — Duplicate prevention: return existing if same period already generated
        Optional<Statement> existing = statementRepository
                .findByAccountIdAndPeriodStartAndPeriodEnd(accountId, periodStart, periodEnd);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<TransactionDTO> allTxns = transactionClient.getTransactionsByAccount(accountId);

        // Bug 1 — Sort by txnDate ascending before filtering so closing balance is correct
        List<TransactionDTO> filtered = allTxns.stream()
                .filter(t -> t.getTxnDate() != null
                        && !t.getTxnDate().toLocalDate().isBefore(periodStart)
                        && !t.getTxnDate().toLocalDate().isAfter(periodEnd))
                .sorted(Comparator.comparing(TransactionDTO::getTxnDate))
                .toList();

        BigDecimal totalDebit = filtered.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getTxnType()))
                .map(TransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = filtered.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getTxnType()))
                .map(TransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bug 1 fix — last item in the sorted list is the latest transaction
        BigDecimal closingBalance = filtered.isEmpty() ? account.getCurrentBalance()
                : filtered.get(filtered.size() - 1).getBalanceAfter();

        // Opening balance = closingBalance - totalCredit + totalDebit
        BigDecimal openingBalance = closingBalance.subtract(totalCredit).add(totalDebit);

        Map<String, Object> summary = new HashMap<>();
        summary.put("accountId",         accountId);
        summary.put("accountNumber",      account.getAccountNumber());
        summary.put("currency",           account.getCurrency());
        summary.put("periodStart",        periodStart.toString());
        summary.put("periodEnd",          periodEnd.toString());
        summary.put("transactionCount",   filtered.size());
        summary.put("openingBalance",     openingBalance);
        summary.put("totalDebit",         totalDebit);
        summary.put("totalCredit",        totalCredit);
        summary.put("closingBalance",     closingBalance);
        summary.put("transactions",       filtered);

        String summaryJson;
        try {
            summaryJson = objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            summaryJson = "{}";
        }

        Statement statement = Statement.builder()
                .accountId(accountId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .summaryJson(summaryJson)
                .build();

        return statementRepository.save(statement);
    }

    public List<Statement> getStatements(Long accountId) {
        return statementRepository.findByAccountIdOrderByGeneratedDateDesc(accountId);
    }

    // Bug 3 — Use ResourceNotFoundException instead of RuntimeException
    public Statement getStatementById(Long statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Statement not found: " + statementId));
    }

    public DepositReport generateDepositReport(String scope) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("scope", scope);
        metrics.put("generatedAt", java.time.LocalDateTime.now().toString());

        try {
            if ("ALL".equalsIgnoreCase(scope)) {
                List<CasaClient.AccountDTO> accounts = casaClient.getAllActiveCasaAccounts();
                BigDecimal totalBalance = accounts.stream()
                        .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                metrics.put("reportType",    "AGGREGATE");
                metrics.put("description",   "Aggregate report across all active deposit accounts");
                metrics.put("totalAccounts", accounts.size());
                metrics.put("totalBalance",  totalBalance);
                metrics.put("savingsCount",  accounts.stream().filter(a -> "SAVINGS".equalsIgnoreCase(a.getCategory())).count());
                metrics.put("currentCount",  accounts.stream().filter(a -> "CURRENT".equalsIgnoreCase(a.getCategory())).count());

            } else if ("CASA".equalsIgnoreCase(scope)) {
                List<CasaClient.AccountDTO> accounts = casaClient.getAllActiveCasaAccounts();
                BigDecimal totalBalance = accounts.stream()
                        .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal savingsBalance = accounts.stream()
                        .filter(a -> "SAVINGS".equalsIgnoreCase(a.getCategory()))
                        .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal currentBalance = accounts.stream()
                        .filter(a -> "CURRENT".equalsIgnoreCase(a.getCategory()))
                        .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                metrics.put("reportType",      "CASA");
                metrics.put("description",     "Current & Savings account deposits report");
                metrics.put("totalAccounts",   accounts.size());
                metrics.put("totalBalance",    totalBalance);
                metrics.put("savingsBalance",  savingsBalance);
                metrics.put("currentBalance",  currentBalance);
                metrics.put("savingsCount",    accounts.stream().filter(a -> "SAVINGS".equalsIgnoreCase(a.getCategory())).count());
                metrics.put("currentCount",    accounts.stream().filter(a -> "CURRENT".equalsIgnoreCase(a.getCategory())).count());

            } else if ("FD".equalsIgnoreCase(scope)) {
                metrics.put("reportType",    "FD");
                metrics.put("description",   "Fixed Deposit portfolio report");
                metrics.put("totalAccounts", 0);
                metrics.put("totalBalance",  BigDecimal.ZERO);
                metrics.put("note",          "FD data available via TD Servicing module");

            } else if ("MONTHLY".equalsIgnoreCase(scope)) {
                List<CasaClient.AccountDTO> accounts = casaClient.getAllActiveCasaAccounts();
                String period = java.time.YearMonth.now().toString();
                BigDecimal totalBalance = accounts.stream()
                        .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                metrics.put("reportType",    "MONTHLY");
                metrics.put("description",   "Monthly deposit activity report");
                metrics.put("period",         period);
                metrics.put("totalAccounts", accounts.size());
                metrics.put("totalBalance",  totalBalance);

            } else {
                // Numeric account ID
                try {
                    Long accountId = Long.parseLong(scope);
                    CasaClient.AccountDTO account = casaClient.getAccountById(accountId);
                    List<TransactionDTO> transactions = transactionClient.getTransactionsByAccount(accountId);

                    BigDecimal totalCredit = transactions.stream()
                            .filter(t -> "CREDIT".equalsIgnoreCase(t.getTxnType()))
                            .map(TransactionDTO::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalDebit = transactions.stream()
                            .filter(t -> "DEBIT".equalsIgnoreCase(t.getTxnType()))
                            .map(TransactionDTO::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    metrics.put("reportType",       "ACCOUNT");
                    metrics.put("accountId",         accountId);
                    metrics.put("accountNumber",     account.getAccountNumber());
                    metrics.put("category",          account.getCategory());
                    metrics.put("currency",          account.getCurrency());
                    metrics.put("transactionCount",  transactions.size());
                    metrics.put("totalCredit",       totalCredit);
                    metrics.put("totalDebit",        totalDebit);
                    metrics.put("totalAccounts",     1);
                    metrics.put("totalBalance",      account.getCurrentBalance() != null
                            ? account.getCurrentBalance() : BigDecimal.ZERO);
                } catch (NumberFormatException e) {
                    metrics.put("reportType",    "CUSTOM");
                    metrics.put("description",   "Custom scope: " + scope);
                    metrics.put("totalAccounts", 0);
                    metrics.put("totalBalance",  BigDecimal.ZERO);
                }
            }
        } catch (Exception e) {
            metrics.put("error", "Failed to fetch data: " + e.getMessage());
            metrics.put("totalAccounts", 0);
            metrics.put("totalBalance", BigDecimal.ZERO);
        }

        String metricsJson;
        try {
            metricsJson = objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException e) {
            metricsJson = "{}";
        }

        DepositReport report = DepositReport.builder()
                .scope(scope)
                .metrics(metricsJson)
                .build();

        return depositReportRepository.save(report);
    }

    public List<DepositReport> getAllReports() {
        return depositReportRepository.findAll();
    }

    // Bug 3 — Use ResourceNotFoundException
    public DepositReport getReportById(Long reportId) {
        return depositReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report not found: " + reportId));
    }
}
