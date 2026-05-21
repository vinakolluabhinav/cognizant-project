package com.depositcorex.Main.Service;

import com.depositcorex.Main.Client.AccountDTO;
import com.depositcorex.Main.Client.CasaAccountClient;
import com.depositcorex.Main.Entities.HoldOrLien;
import com.depositcorex.Main.Entities.StandingInstruction;
import com.depositcorex.Main.Exception.InvalidTransactionException;
import com.depositcorex.Main.Exception.ResourceNotFoundException;
import com.depositcorex.Main.Repository.HoldRepository;
import com.depositcorex.Main.Repository.StandingInstructionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service layer responsible for processing core banking deposit workflows.
 * It manages account holds/liens and configures automated standing instructions.
 */
@Service
public class ServicingService {

    // Final immutable dependencies required for business operations
    private final HoldRepository holdRepo;
    private final StandingInstructionRepository siRepo;
    private final CasaAccountClient casaAccountClient; // Feign Client communicating with external Account microservice

    // Constructor Injection (Enables clean dependency management and straightforward unit testing)
    public ServicingService(HoldRepository holdRepo,
                            StandingInstructionRepository siRepo,
                            CasaAccountClient casaAccountClient) {
        this.holdRepo = holdRepo;
        this.siRepo = siRepo;
        this.casaAccountClient = casaAccountClient;
    }

    /**
     * Places an active financial hold or legal lien against an account's funds.
     * * @param accountId Target identity key of the account
     * @param amount    The financial sum to freeze
     * @param reason    Context/Justification (e.g., "Court order", "Pending debit verify")
     * @param type      Categorization tag (e.g., "HOLD" or "LIEN")
     * @return The saved HoldOrLien record entity
     */
    @Transactional // Executes within a transaction context; rolls back database records if an error occurs
    public HoldOrLien placeHold(Long accountId, BigDecimal amount, String reason, String type) {
        // Step 1: Query the external microservice via Feign Client to verify if the account exists
        AccountDTO account = casaAccountClient.getAccountById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account ID " + accountId + " not found");
        }

        // Step 2: Build a new Hold domain entity state profile
        HoldOrLien hold = new HoldOrLien();
        hold.setAccountId(accountId);
        hold.setAmount(amount);
        hold.setHoldType(type);
        hold.setReason(reason);
        hold.setStatus("ACTIVE"); // Holds default immediately to an ACTIVE status state
        hold.setPlacedDate(LocalDateTime.now()); // Set timestamp to current operational runtime
        
        // Step 3: Persist the hold configuration state inside the database
        return holdRepo.save(hold);
    }

    /**
     * Unfreezes/releases a previously active hold record, freeing up the account's available balance.
     * * @param holdId The specific primary tracking key of the hold entity record
     */
    @Transactional
    public void releaseHold(Long holdId) {
        // Step 1: Look up the existing hold. If it doesn't exist, throw a 404 tracking exception
        HoldOrLien hold = holdRepo.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold record ID " + holdId + " not found"));
        
        // Step 2: Transition the tracking lifecycle status flag to RELEASED
        hold.setStatus("RELEASED");
        
        // Step 3: Save updated metadata alterations back to our repository layer
        holdRepo.save(hold);
    }

    /**
     * Calculates the real-time spendable funds of an account using the formula:
     * Available Balance = Current Balance - Sum of Active Holds
     * * @param accountId Unique target account ID
     * @return BigDecimal remaining spendable balance
     */
    public BigDecimal getAvailableBalance(Long accountId) {
        // Step 1: Retrieve account ledger limits from external core system via Feign Client
        AccountDTO account = casaAccountClient.getAccountById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }

        // Step 2: Run custom JPQL query to sum all active holds mapped to this profile
        BigDecimal totalHolds = holdRepo.sumActiveHolds(accountId);
        if (totalHolds == null) totalHolds = BigDecimal.ZERO; // Handle database null results if no active rows exist

        // Step 3: Handle null values safely, then compute the math to get the final spendable amount
        BigDecimal balance = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
        return balance.subtract(totalHolds);
    }

    /**
     * Establishes a recurring automated transfer rule (Standing Instruction) between two distinct accounts.
     * * @param fromAccId Origin account supplying the funds
     * @param toAccId   Destination account receiving the funds
     * @param amount    The value to transfer on each cycle
     * @param frequency Scheduling window code (e.g., "DAILY", "WEEKLY", "MONTHLY")
     * @return The persisted StandingInstruction tracking descriptor
     */
    @Transactional
    public StandingInstruction createStandingInstruction(Long fromAccId, Long toAccId,
                                                         BigDecimal amount, String frequency) {
        // Validates existential footprints of both internal account entities via API calls before proceeding
        casaAccountClient.getAccountById(fromAccId);
        casaAccountClient.getAccountById(toAccId);

        // Instantiates and maps structural operational metadata fields
        StandingInstruction si = new StandingInstruction();
        si.setFromAccountId(fromAccId);
        si.setToAccountId(toAccId);
        si.setAmount(amount);
        si.setFrequency(frequency);
        si.setNextRunDate(LocalDate.now()); // Set the initial execution date to today
        si.setStatus("ACTIVE");
        
        return siRepo.save(si);
    }

    /**
     * Helper validation routine to assert whether an account has enough spendable funds for a debit request.
     * * @param accountId   Target checking account instance
     * @param debitAmount The requested withdrawal/debit amount
     * @throws InvalidTransactionException if the requested amount exceeds available funds
     */
    public void validateFundAvailability(Long accountId, BigDecimal debitAmount) {
        BigDecimal available = getAvailableBalance(accountId);
        
        // Compare values: if debitAmount > available balance, reject execution parameters
        if (debitAmount.compareTo(available) > 0) {
            throw new InvalidTransactionException("Insufficient available funds. Required: " + debitAmount +
                    ", Available: " + available);
        }
    }

    /**
     * Helper validation routine to assert that an account's operational status allows Standing Instructions to execute.
     * Block setups on accounts that are Closed, Dormant, or otherwise inactive.
     * * @param accountId Unique target account ID
     * @throws InvalidTransactionException if the account status is invalid for processing automated workflows
     */
    public void validateAccountStatusForSI(Long accountId) {
        AccountDTO account = casaAccountClient.getAccountById(accountId);
        String status = account.getStatus();
        
        // Block processing if the account has a terminal state (CLOSED) or is frozen due to inactivity (DORMANT)
        if ("CLOSED".equalsIgnoreCase(status) || "DORMANT".equalsIgnoreCase(status)) {
            throw new InvalidTransactionException("SI Execution Failed: Account " +
                    account.getAccountNumber() + " is " + status);
        }
        
        // Fail-safe catch-all constraint validation requirement: Account state must be explicitly set to ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new InvalidTransactionException("SI Execution Failed: Account must be ACTIVE.");
        }
    }
}