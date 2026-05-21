package com.depositcorex.Main.SIExecutionJob;

import com.depositcorex.Main.Client.CasaAccountClient;
import com.depositcorex.Main.Client.AccountDTO;
import com.depositcorex.Main.Client.CustomerClient;
import com.depositcorex.Main.Client.NotificationClient;
import com.depositcorex.Main.Client.TransactionClient;
import com.depositcorex.Main.Client.TransactionRequestDTO;
import com.depositcorex.Main.Entities.StandingInstruction;
import com.depositcorex.Main.Repository.StandingInstructionRepository;
import com.depositcorex.Main.Service.ServicingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class StandingInstructionJob {

    private final StandingInstructionRepository siRepo;
    private final ServicingService              servicingService;
    private final TransactionClient             transactionClient;
    private final CasaAccountClient             casaAccountClient;
    private final CustomerClient                customerClient;
    private final NotificationClient            notificationClient;

    @Scheduled(cron = "0 31 14 * * *")
    public void processDailySIs() {
        List<StandingInstruction> dueSIs =
                siRepo.findByStatusAndNextRunDateLessThanEqual("ACTIVE", LocalDate.now());

        log.info("[SI Scheduler] Processing {} due standing instructions", dueSIs.size());

        for (StandingInstruction si : dueSIs) {
            try {
                executeSingleSI(si);
                log.info("[SI Scheduler] SI ID {} executed successfully", si.getSiId());
            } catch (Exception e) {
                log.error("[SI Scheduler] Failed SI ID: {}. Error: {}", si.getSiId(), e.getMessage());
                si.setStatus("FAILED");
                siRepo.save(si);
                // Notify account holder of failure
                notifyFailure(si);
            }
        }
    }

    private void executeSingleSI(StandingInstruction si) {
        servicingService.validateAccountStatusForSI(si.getFromAccountId());
        servicingService.validateAccountStatusForSI(si.getToAccountId());
        servicingService.validateFundAvailability(si.getFromAccountId(), si.getAmount());

        // Debit source account
        transactionClient.postTransaction(new TransactionRequestDTO(
                si.getFromAccountId(), "DEBIT", si.getAmount(),
                "SI Transfer to account: " + si.getToAccountId(), "SYSTEM", LocalDateTime.now()));

        // Credit destination account
        transactionClient.postTransaction(new TransactionRequestDTO(
                si.getToAccountId(), "CREDIT", si.getAmount(),
                "SI Transfer from account: " + si.getFromAccountId(), "SYSTEM", LocalDateTime.now()));

        updateNextRunDate(si);
        siRepo.save(si);

        // L15 — Notify both account holders after successful SI execution
        notifySuccess(si);
    }

    private void notifySuccess(StandingInstruction si) {
        // Notify source account holder (debit)
        notifyAccount(si.getFromAccountId(),
                "Standing Instruction executed: " + si.getAmount()
                + " debited from your account and transferred to account "
                + si.getToAccountId() + ".",
                "SI");

        // Notify destination account holder (credit)
        notifyAccount(si.getToAccountId(),
                "Standing Instruction executed: " + si.getAmount()
                + " credited to your account from account "
                + si.getFromAccountId() + ".",
                "SI");
    }

    private void notifyFailure(StandingInstruction si) {
        notifyAccount(si.getFromAccountId(),
                "Standing Instruction (ID: " + si.getSiId() + ") failed to execute. "
                + "Please check your account balance or contact support.",
                "SI");
    }

    private void notifyAccount(Long accountId, String message, String category) {
        try {
            AccountDTO account = casaAccountClient.getAccountById(accountId);
            if (account.getCustomerId() == null) return;

            CustomerClient.CustomerDTO customer =
                    customerClient.getByCustomerId(account.getCustomerId());
            if (customer.getUserId() == null) return;

            notificationClient.sendNotification(
                    new NotificationClient.NotificationRequest(
                            customer.getUserId(), message, category));
        } catch (Exception e) {
            log.warn("[SI Scheduler] Failed to send notification for account {}: {}",
                    accountId, e.getMessage());
        }
    }

    private void updateNextRunDate(StandingInstruction si) {
        switch (si.getFrequency().toUpperCase()) {
            case "MONTHLY"          -> si.setNextRunDate(si.getNextRunDate().plusMonths(1));
            case "WEEKLY"           -> si.setNextRunDate(si.getNextRunDate().plusWeeks(1));
            case "DAILY"            -> si.setNextRunDate(si.getNextRunDate().plusDays(1));
            case "ONCE", "ONE_TIME" -> si.setStatus("COMPLETED");
            default                 -> si.setStatus("FAILED");
        }
    }
}
