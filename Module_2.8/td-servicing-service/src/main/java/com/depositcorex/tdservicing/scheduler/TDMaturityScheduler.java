package com.depositcorex.tdservicing.scheduler;

import com.depositcorex.tdservicing.client.CasaClient;
import com.depositcorex.tdservicing.client.TermDepositDTO;
import com.depositcorex.tdservicing.repository.TDMaturityRepository;
import com.depositcorex.tdservicing.service.TdServicingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs daily at 08:00 AM.
 * Finds all ACTIVE Term Deposits whose MaturityDate == today
 * and automatically processes them as PAYOUT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TDMaturityScheduler {

    private final CasaClient          casaClient;
    private final TdServicingService  tdServicingService;
    private final TDMaturityRepository maturityRepo;

    @Scheduled(cron = "0 0 8 * * *")
    public void processMaturingTDs() {
        log.info("[TDMaturityScheduler] Checking for TDs maturing today...");

        List<TermDepositDTO> maturingTDs;
        try {
            maturingTDs = casaClient.getTdsMaturingToday();
        } catch (Exception e) {
            log.error("[TDMaturityScheduler] Failed to fetch maturing TDs: {}", e.getMessage());
            return;
        }

        if (maturingTDs.isEmpty()) {
            log.info("[TDMaturityScheduler] No TDs maturing today.");
            return;
        }

        log.info("[TDMaturityScheduler] Found {} TD(s) maturing today", maturingTDs.size());
        int success = 0, skipped = 0, failed = 0;

        for (TermDepositDTO td : maturingTDs) {
            try {
                // Skip if already processed
                boolean done = maturityRepo.findByTdId(td.getTdId()).stream()
                        .anyMatch(m -> "PAID".equals(m.getStatus()) || "RENEWED".equals(m.getStatus()));
                if (done) { skipped++; continue; }

                // System-initiated payout (userId = 0)
                tdServicingService.processMaturity(td.getTdId(), "PAYOUT", 0L);
                log.info("[TDMaturityScheduler] TD {} processed", td.getTdId());
                success++;
            } catch (Exception e) {
                log.error("[TDMaturityScheduler] Failed TD {}: {}", td.getTdId(), e.getMessage());
                failed++;
            }
        }

        log.info("[TDMaturityScheduler] Done — Success: {}, Skipped: {}, Failed: {}",
                success, skipped, failed);
    }
}
