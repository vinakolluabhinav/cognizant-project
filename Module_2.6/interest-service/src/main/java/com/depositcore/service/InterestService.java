package com.depositcore.service;

import com.depositcore.client.AccountDTO;
import com.depositcore.client.CasaAccountClient;
import com.depositcore.client.TransactionClient;
import com.depositcore.dto.InterestRequestDTO;
import com.depositcore.dto.InterestResponseDTO;
import com.depositcore.entity.InterestAccrual;
import com.depositcore.entity.InterestPosting;
import com.depositcore.entity.PostingType;
import com.depositcore.exception.ResourceNotFoundException;
import com.depositcore.repository.InterestAccrualRepository;
import com.depositcore.repository.InterestPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestService {

    private final InterestAccrualRepository accrualRepo;
    private final InterestPostingRepository postingRepo;
    private final CasaAccountClient         casaAccountClient;
    private final TransactionClient         transactionClient;

    // ── Calculation ──────────────────────────────────────────────

    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal annualRatePct,
                                         long days, String method) {
        if ("COMPOUNDED".equalsIgnoreCase(method)) {
            double r = annualRatePct.doubleValue() / 36500.0;
            return BigDecimal.valueOf(principal.doubleValue() * (Math.pow(1 + r, days) - 1))
                    .setScale(4, RoundingMode.HALF_UP);
        }
        // SIMPLE (default)
        return principal
                .multiply(annualRatePct)
                .multiply(BigDecimal.valueOf(days))
                .divide(new BigDecimal("36500"), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal deriveRate(BigDecimal principal, BigDecimal explicitRate) {
        if (explicitRate != null && explicitRate.compareTo(BigDecimal.ZERO) > 0) return explicitRate;
        // Default slabs — replace with Product Config lookup in Phase 2
        if (principal.compareTo(new BigDecimal("10000")) <= 0) return new BigDecimal("3.0000");
        if (principal.compareTo(new BigDecimal("50000")) <= 0) return new BigDecimal("4.0000");
        return new BigDecimal("5.0000");
    }

    // ── Accrual ──────────────────────────────────────────────────

    @Transactional
    public InterestResponseDTO createAccrual(InterestRequestDTO request) {
        if (!request.getPeriodEnd().isAfter(request.getPeriodStart()))
            throw new IllegalStateException("periodEnd must be after periodStart");

        AccountDTO account = casaAccountClient.getAccountById(request.getAccountId());
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus()))
            throw new IllegalStateException("Cannot accrue on non-active account: " + request.getAccountId());

        BigDecimal principal = account.getCurrentBalance();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("Zero/null balance — nothing to accrue: " + request.getAccountId());

        // L13 — duplicate prevention
        if (accrualRepo.existsByAccountIdAndPeriodStartLessThanAndPeriodEndGreaterThan(
                request.getAccountId(), request.getPeriodEnd(), request.getPeriodStart())) {
            throw new IllegalStateException("Overlapping accrual already exists for account "
                    + request.getAccountId());
        }

        long days           = ChronoUnit.DAYS.between(request.getPeriodStart(), request.getPeriodEnd());
        String method       = request.getInterestMethod() != null ? request.getInterestMethod() : "SIMPLE";
        BigDecimal rate     = deriveRate(principal, request.getAnnualRatePct());
        BigDecimal interest = calculateInterest(principal, rate, days, method);
        String currency     = account.getCurrency() != null ? account.getCurrency() : "INR";

        InterestAccrual accrual = InterestAccrual.builder()
                .accountId(request.getAccountId())
                .productId(request.getProductId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .principal(principal)
                .rateApplied(rate)
                .interestMethod(method)
                .interestAmount(interest)
                .currency(currency)
                .status("PENDING")
                .calculatedDate(LocalDateTime.now())
                .build();

        accrualRepo.save(accrual);

        return InterestResponseDTO.builder()
                .accountId(request.getAccountId())
                .interestAmount(interest)
                .message("Accrued " + interest + " " + currency + " @ " + rate
                        + "% (" + method + ") for " + days + " days ["
                        + request.getPeriodStart() + " → " + request.getPeriodEnd() + "]")
                .build();
    }

    // ── Posting ──────────────────────────────────────────────────

    /**
     * Posts all PENDING accruals for an account.
     * Step 4-6 of the workflow:
     *   1. Sum all PENDING accruals
     *   2. Call Transaction service to CREDIT the account → get txnId
     *   3. Save InterestPosting with the txnId for GL linkage
     *   4. Mark all accruals as POSTED
     */
    @Transactional
    public InterestResponseDTO postInterest(Long accountId, PostingType postingType) {
        List<InterestAccrual> pending = accrualRepo.findByAccountIdAndStatus(accountId, "PENDING");
        if (pending.isEmpty())
            throw new ResourceNotFoundException("No pending accruals for account: " + accountId);

        BigDecimal totalInterest = pending.stream()
                .map(InterestAccrual::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency  = pending.get(0).getCurrency();
        LocalDate from   = pending.stream().map(InterestAccrual::getPeriodStart).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate to     = pending.stream().map(InterestAccrual::getPeriodEnd).max(LocalDate::compareTo).orElse(LocalDate.now());

        // Step 5 — Feign call to Transaction service to credit account
        // This creates a proper Transaction + GL Posting record
        Long txnId = null;
        try {
            TransactionClient.TransactionResponseDTO txn = transactionClient.postTransaction(
                    new TransactionClient.TransactionRequestDTO(
                            accountId, "CREDIT", totalInterest,
                            postingType.name() + " — Period: " + from + " to " + to,
                            "SYSTEM", LocalDateTime.now()));
            txnId = txn != null ? txn.getTxnId() : null;
            log.info("[InterestService] Transaction #{} created for interest posting on account {}", txnId, accountId);
        } catch (Exception e) {
            log.error("[InterestService] Failed to post transaction for account {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Transaction service call failed: " + e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        final Long finalTxnId = txnId;

        // Step 6 — Save InterestPosting with txnId, mark all accruals as POSTED
        for (InterestAccrual accrual : pending) {
            postingRepo.save(InterestPosting.builder()
                    .accountId(accountId)
                    .accrualId(accrual.getAccrualId())
                    .amount(accrual.getInterestAmount())
                    .currency(accrual.getCurrency())
                    .postingDate(now)
                    .postingType(postingType)
                    .glTransactionId(finalTxnId)   // L7 — GL linkage
                    .build());

            accrual.setStatus("POSTED");
            accrual.setPostedDate(now);
            accrualRepo.save(accrual);
        }

        return InterestResponseDTO.builder()
                .accountId(accountId)
                .interestAmount(totalInterest)
                .postedCount(pending.size())
                .message(pending.size() + " accrual(s) posted — " + totalInterest
                        + " " + currency + " credited via Txn #" + txnId)
                .build();
    }
}
