package com.depositcore.scheduler;

import com.depositcore.client.AccountDTO;
import com.depositcore.client.CasaAccountClient;
import com.depositcore.dto.InterestRequestDTO;
import com.depositcore.entity.PostingType;
import com.depositcore.repository.InterestAccrualRepository;
import com.depositcore.service.InterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterestAccrualScheduler {

    private final InterestAccrualRepository accrualRepo;
    private final InterestService           interestService;
    private final CasaAccountClient         casaAccountClient;

    /**
     * Job A — CASA Daily Accrual
     * Runs at 23:59 every night.
     * Fetches ALL active CASA (SAVINGS/CURRENT) accounts from CASA service
     * and accrues 1 day of interest on each.
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void runDailyCasaAccrual() {
        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        log.info("[InterestScheduler] CASA daily accrual starting — period {} → {}", yesterday, today);

        List<AccountDTO> accounts;
        try {
            accounts = casaAccountClient.getAllActiveCasaAccounts();
        } catch (Exception e) {
            log.error("[InterestScheduler] Failed to fetch CASA accounts: {}", e.getMessage());
            return;
        }

        int success = 0, skipped = 0, failed = 0;

        for (AccountDTO account : accounts) {
            try {
                // Skip if already accrued for today
                boolean alreadyAccrued = accrualRepo
                        .existsByAccountIdAndPeriodStartLessThanAndPeriodEndGreaterThan(
                                account.getAccountId(), today, yesterday);
                if (alreadyAccrued) { skipped++; continue; }

                // Skip zero-balance accounts
                if (account.getCurrentBalance() == null ||
                    account.getCurrentBalance().signum() <= 0) { skipped++; continue; }

                InterestRequestDTO req = new InterestRequestDTO();
                req.setAccountId(account.getAccountId());
                req.setPeriodStart(yesterday);
                req.setPeriodEnd(today);
                req.setInterestMethod("SIMPLE");
                // Rate derived from balance slab in InterestService

                interestService.createAccrual(req);
                success++;
            } catch (Exception e) {
                log.error("[InterestScheduler] Accrual failed for account {}: {}",
                        account.getAccountId(), e.getMessage());
                failed++;
            }
        }

        log.info("[InterestScheduler] CASA accrual done — Success: {}, Skipped: {}, Failed: {}",
                success, skipped, failed);
    }

    /**
     * Job B — Monthly CASA Interest Posting
     * Runs on the last day of each month at 23:55.
     * Posts (credits) all accumulated PENDING accruals for every active CASA account.
     */
    @Scheduled(cron = "0 55 23 L * *")
    public void runMonthlyCasaPosting() {
        log.info("[InterestScheduler] Monthly CASA interest posting starting");

        List<Long> accountIds = accrualRepo.findAccountIdsWithPendingAccruals();
        int success = 0, failed = 0;

        for (Long accountId : accountIds) {
            try {
                interestService.postInterest(accountId, PostingType.CASAInterest);
                success++;
            } catch (Exception e) {
                log.error("[InterestScheduler] Posting failed for account {}: {}",
                        accountId, e.getMessage());
                failed++;
            }
        }

        log.info("[InterestScheduler] Monthly posting done — Success: {}, Failed: {}", success, failed);
    }
}
