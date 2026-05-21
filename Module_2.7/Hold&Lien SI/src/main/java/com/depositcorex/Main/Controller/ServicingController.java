package com.depositcorex.Main.Controller;

import com.depositcorex.Main.Dto.BalanceResponse;
import com.depositcorex.Main.Dto.HoldRequest;
import com.depositcorex.Main.Dto.SIRequest;
import com.depositcorex.Main.Entities.HoldOrLien;
import com.depositcorex.Main.Entities.StandingInstruction;
import com.depositcorex.Main.Service.ServicingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller that handles core deposit operation requests such as placing/releasing holds,
 * calculating available balances, and configuring standing instructions.
 * * It serves as the API entry point, validating roles before executing business logic.
 */
@RestController
public class ServicingController {

    private final ServicingService servicingService;

    // Constructor-based dependency injection to wire in the business logic service layer
    public ServicingController(ServicingService servicingService) {
        this.servicingService = servicingService;
    }

    /**
     * POST Endpoint: Places a financial hold or lien on a specific account.
     *
     * @return ResponseEntity containing the newly generated HoldOrLien record with an HTTP status 201 Created
     */
    @PostMapping("/api/v1/holds/place")
    // Method-level security constraint: Restricts endpoint access exclusively to bank staff or admin roles
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<HoldOrLien> placeHold(@RequestBody HoldRequest request) {
        HoldOrLien hold = servicingService.placeHold(
            request.getAccountId(),
            request.getAmount(),
            request.getReason(),
            request.getType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    /**
     * POST Endpoint: Removes an existing hold or lien record from an account, changing its state.
     * 
     * @return ResponseEntity carrying a plain-text confirmation message with an HTTP status 200 OK
     */
    @PostMapping("/api/v1/holds/release/{holdId}")
    // Security restriction: Regular customers cannot manually release holds or liens placed on their accounts
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<String> releaseHold(@PathVariable Long holdId) {
        servicingService.releaseHold(holdId);
        return ResponseEntity.ok("Hold released successfully");
    }

    /**
     * GET Endpoint: Calculates the available balance of an account (Current Balance minus Active Holds).
     * 
     * @return ResponseEntity enclosing a structured BalanceResponse DTO with an HTTP status 200 OK
     */
    @GetMapping("/api/v1/holds/balance/{accountId}")
    // Broader access profile: Allows both the customer and analytical/operational backoffice users to check balances
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<BalanceResponse> getAvailableBalance(@PathVariable Long accountId) {
        BigDecimal available = servicingService.getAvailableBalance(accountId);
        return ResponseEntity.ok(new BalanceResponse(accountId, available));
    }

    /**
     * POST Endpoint: Sets up automated, recurring fund transfers (Standing Instructions) between two internal accounts.
     * 
     * @return ResponseEntity with the persisted StandingInstruction meta-data entity alongside an HTTP status 200 OK
     */
    @PostMapping("/api/v1/standing-instructions")
    // Permission Matrix: Allows customers to automate their own workflows, alongside general back-office administrators
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<StandingInstruction> createSI(@RequestBody SIRequest request) {
        StandingInstruction si = servicingService.createStandingInstruction(
            request.getFromAccId(),
            request.getToAccId(),
            request.getAmount(),
            request.getFrequency()
        );
        return ResponseEntity.ok(si);
    }
}