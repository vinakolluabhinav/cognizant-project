package com.depositcorex.tdservicing.service;

import com.depositcorex.tdservicing.client.*;
import com.depositcorex.tdservicing.entity.PrematureClosure;
import com.depositcorex.tdservicing.entity.TDMaturity;
import com.depositcorex.tdservicing.exception.ResourceNotFoundException;
import com.depositcorex.tdservicing.repository.PrematureClosureRepository;
import com.depositcorex.tdservicing.repository.TDMaturityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TdServicingService {

    private final TDMaturityRepository      maturityRepo;
    private final PrematureClosureRepository closureRepo;
    private final CasaClient                casaClient;
    private final TransactionClient         transactionClient;
    private final NotificationClient        notificationClient;
    private final InterestClient            interestClient;

    @Transactional
    public TDMaturity processMaturity(Long tdId, String renewalChoice, Long initiatedByUserId) {
        TermDepositDTO td = casaClient.getTermDepositByTdId(tdId);

        // Guard: must be ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(td.getStatus())) {
            throw new IllegalStateException("Term Deposit is not active: " + tdId);
        }

        // Guard: maturity date must have been reached
        if (td.getMaturityDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException(
                    "TD has not yet matured. Maturity date: " + td.getMaturityDate());
        }

        // T2 — prevent double-processing (unique constraint + explicit check)
        if (maturityRepo.findByTdId(tdId).stream()
                .anyMatch(m -> "PAID".equals(m.getStatus()) || "RENEWED".equals(m.getStatus()))) {
            throw new IllegalStateException("Maturity already processed for TD: " + tdId);
        }

        BigDecimal rate     = td.getRate() != null ? td.getRate() : BigDecimal.valueOf(5.0);
        long days           = ChronoUnit.DAYS.between(td.getStartDate(), td.getMaturityDate());

        // Use accrued interest from Module 2.6 if available, else recalculate
        BigDecimal interest = getAccruedInterest(td.getAccountId());
        if (interest.compareTo(BigDecimal.ZERO) == 0) {
            // Fallback: recalculate using simple interest
            interest = td.getPrincipalAmount()
                    .multiply(rate)
                    .multiply(BigDecimal.valueOf(days))
                    .divide(BigDecimal.valueOf(36500), 4, RoundingMode.HALF_UP);
            log.info("[TDMaturity] No accruals found for account {}, using recalculated interest", td.getAccountId());
        } else {
            log.info("[TDMaturity] Using {} accrued interest from Interest Service for account {}", interest, td.getAccountId());
        }
        BigDecimal maturityAmount = td.getPrincipalAmount().add(interest);

        // T6 — currency from account
        CasaClient.AccountDTO account = casaClient.getAccountById(td.getAccountId());
        String currency = account.getCurrency() != null ? account.getCurrency() : "INR";

        Long payoutTxnId = null;
        String status;

        if ("RENEW".equalsIgnoreCase(renewalChoice)) {
            status = "RENEWED";
            casaClient.updateTermDepositStatus(tdId, "RENEWED");
        } else {
            // Post credit transaction and capture txnId (L5)
            TransactionResponseDTO txn = transactionClient.postTransaction(
                    new TransactionRequestDTO(
                            td.getAccountId(), "CREDIT", maturityAmount,
                            "TD Maturity Payout — TDID: " + tdId, "SYSTEM", LocalDateTime.now()));
            payoutTxnId = txn != null ? txn.getTxnId() : null;
            casaClient.updateTermDepositStatus(tdId, "MATURED");
            status = "PAID";
        }

        TDMaturity maturity = TDMaturity.builder()
                .tdId(tdId)
                .principalAmount(td.getPrincipalAmount())   // L3 equivalent
                .interestAmount(interest)
                .maturityAmount(maturityAmount)
                .currency(currency)                          // T6
                .payoutDate(LocalDate.now())
                .status(status)
                .payoutTxnId(payoutTxnId)                   // L5
                .initiatedByUserId(initiatedByUserId)        // T5
                .build();

        TDMaturity saved = maturityRepo.save(maturity);

        // L15 — Send notification to customer
        sendNotification(account.getCustomerId(),
                "RENEW".equalsIgnoreCase(renewalChoice)
                    ? "Your Fixed Deposit (ID: " + tdId + ") has been renewed successfully."
                    : "Your Fixed Deposit (ID: " + tdId + ") has matured. "
                      + currency + " " + maturityAmount + " has been credited to your account.",
                "Maturity");

        return saved;
    }

    @Transactional
    public PrematureClosure closePremature(Long tdId, BigDecimal penalRate, Long initiatedByUserId) {
        TermDepositDTO td = casaClient.getTermDepositByTdId(tdId);

        // Guard: must be ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(td.getStatus())) {
            throw new IllegalStateException("Term Deposit is not active: " + tdId);
        }

        // L14 — guard: cannot close already-matured TD
        if ("MATURED".equalsIgnoreCase(td.getStatus()) || "CLOSED".equalsIgnoreCase(td.getStatus())) {
            throw new IllegalStateException(
                    "Cannot prematurely close a TD with status: " + td.getStatus());
        }

        // T3 — prevent duplicate closure
        if (closureRepo.findByTdId(tdId).stream()
                .anyMatch(c -> "CLOSED".equals(c.getStatus()))) {
            throw new IllegalStateException("Term Deposit already closed prematurely: " + tdId);
        }

        long daysElapsed = ChronoUnit.DAYS.between(td.getStartDate(), LocalDate.now());

        // L4 — capture original rate at closure time
        BigDecimal originalRate   = td.getRate() != null ? td.getRate() : BigDecimal.valueOf(3.0);
        BigDecimal effectiveRate  = originalRate.subtract(penalRate);
        if (effectiveRate.compareTo(BigDecimal.ZERO) < 0) effectiveRate = BigDecimal.ZERO;

        // L3 — itemized payout breakdown using accrued interest from Module 2.6
        BigDecimal totalAccruedInterest = getAccruedInterest(td.getAccountId());

        BigDecimal earnedInterest;
        BigDecimal grossInterest;
        if (totalAccruedInterest.compareTo(BigDecimal.ZERO) > 0) {
            // Use actual accrued interest, apply penal reduction proportionally
            grossInterest  = totalAccruedInterest;
            BigDecimal penalFraction = penalRate.divide(originalRate.compareTo(BigDecimal.ZERO) > 0
                    ? originalRate : BigDecimal.ONE, 4, RoundingMode.HALF_UP);
            BigDecimal penalDed = totalAccruedInterest.multiply(penalFraction).setScale(4, RoundingMode.HALF_UP);
            earnedInterest = totalAccruedInterest.subtract(penalDed);
        } else {
            // Fallback: recalculate
            earnedInterest = td.getPrincipalAmount()
                    .multiply(effectiveRate)
                    .multiply(BigDecimal.valueOf(daysElapsed))
                    .divide(BigDecimal.valueOf(36500), 4, RoundingMode.HALF_UP);
            grossInterest = td.getPrincipalAmount()
                    .multiply(originalRate)
                    .multiply(BigDecimal.valueOf(daysElapsed))
                    .divide(BigDecimal.valueOf(36500), 4, RoundingMode.HALF_UP);
        }

        BigDecimal penalDeduction = grossInterest.subtract(earnedInterest);
        BigDecimal netPayout      = td.getPrincipalAmount().add(earnedInterest);

        // T6 — currency
        CasaClient.AccountDTO account = casaClient.getAccountById(td.getAccountId());
        String currency = account.getCurrency() != null ? account.getCurrency() : "INR";

        // Post credit transaction (L5)
        TransactionResponseDTO txn = transactionClient.postTransaction(
                new TransactionRequestDTO(
                        td.getAccountId(), "CREDIT", netPayout,
                        "Premature Closure Payout — TDID: " + tdId, "SYSTEM", LocalDateTime.now()));
        Long payoutTxnId = txn != null ? txn.getTxnId() : null;

        // L11 — update CASA with PREMATURELY_CLOSED status
        casaClient.updateTermDepositStatus(tdId, "PREMATURELY_CLOSED");

        PrematureClosure closure = PrematureClosure.builder()
                .tdId(tdId)
                .closureDate(LocalDate.now())
                .principalAmount(td.getPrincipalAmount())  // L3
                .earnedInterest(earnedInterest)             // L3
                .penalDeduction(penalDeduction)             // L3
                .netPayout(netPayout)                       // L3 / T4
                .originalRate(originalRate)                 // L4
                .penalRate(penalRate)                       // L4
                .effectiveRate(effectiveRate)               // L4
                .currency(currency)                         // T6
                .status("CLOSED")                           // L8
                .payoutTxnId(payoutTxnId)                   // L5
                .initiatedByUserId(initiatedByUserId)       // T5
                .build();

        PrematureClosure saved = closureRepo.save(closure);

        // L15 — notify customer
        sendNotification(account.getCustomerId(),
                "Your Fixed Deposit (ID: " + tdId + ") has been closed prematurely. "
                + currency + " " + netPayout + " has been credited to your account "
                + "(Penalty applied: " + penalDeduction + ").",
                "Maturity");

        return saved;
    }

    public TDMaturity getMaturityByTdId(Long tdId) {
        return maturityRepo.findByTdId(tdId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maturity record not found for TD: " + tdId));
    }

    public PrematureClosure getClosureByTdId(Long tdId) {
        return closureRepo.findByTdId(tdId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Closure record not found for TD: " + tdId));
    }

    // L15 — fire notification silently
    private void sendNotification(Long customerId, String message, String category) {
        if (customerId == null) return;
        try {
            notificationClient.sendNotification(
                    new NotificationClient.NotificationRequest(customerId, message, category));
        } catch (Exception e) {
            log.warn("Failed to send notification to customer {}: {}", customerId, e.getMessage());
        }
    }

    /**
     * Fetches total PENDING + POSTED accrued interest from Module 2.6.
     * Returns ZERO if Interest service is unavailable (graceful degradation).
     */
    private BigDecimal getAccruedInterest(Long accountId) {
        try {
            return interestClient.getAccrualsByAccount(accountId).stream()
                    .map(InterestClient.AccrualDTO::getInterestAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.warn("[TDServicing] Could not fetch accruals from Interest service for account {}: {}",
                    accountId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
