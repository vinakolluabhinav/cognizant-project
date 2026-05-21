package com.depositcorex.tdservicing.controller;

import com.depositcorex.tdservicing.entity.PrematureClosure;
import com.depositcorex.tdservicing.entity.TDMaturity;
import com.depositcorex.tdservicing.repository.PrematureClosureRepository;
import com.depositcorex.tdservicing.repository.TDMaturityRepository;
import com.depositcorex.tdservicing.service.TdServicingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/td")
@RequiredArgsConstructor
public class TdServicingController {

    private final TdServicingService         tdServicingService;
    private final TDMaturityRepository       maturityRepo;
    private final PrematureClosureRepository closureRepo;

    @PostMapping("/{tdId}/maturity")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<TDMaturity> processMaturity(
            @PathVariable Long tdId,
            @RequestParam(defaultValue = "PAYOUT") String action,
            Authentication auth) {
        Long userId = parseLong(auth.getName());
        return ResponseEntity.ok(tdServicingService.processMaturity(tdId, action, userId));
    }

    @PostMapping("/{tdId}/premature-closure")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<PrematureClosure> closePremature(
            @PathVariable Long tdId,
            @RequestParam(defaultValue = "1.0") BigDecimal penalRate,
            Authentication auth) {
        Long userId = parseLong(auth.getName());
        return ResponseEntity.ok(tdServicingService.closePremature(tdId, penalRate, userId));
    }

    @GetMapping("/{tdId}/maturity")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<TDMaturity> getMaturity(@PathVariable Long tdId) {
        return ResponseEntity.ok(tdServicingService.getMaturityByTdId(tdId));
    }

    @GetMapping("/{tdId}/closure")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<PrematureClosure> getClosure(@PathVariable Long tdId) {
        return ResponseEntity.ok(tdServicingService.getClosureByTdId(tdId));
    }

    private Long parseLong(String val) {
        try { return Long.parseLong(val); }
        catch (NumberFormatException e) { return null; }
    }

    @GetMapping("/maturities")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<TDMaturity>> getAllMaturities() {
        return ResponseEntity.ok(maturityRepo.findAll());
    }

    @GetMapping("/closures")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<PrematureClosure>> getAllClosures() {
        return ResponseEntity.ok(closureRepo.findAll());
    }
}
