package com.depositcore.controller;

import com.depositcore.dto.InterestRequestDTO;
import com.depositcore.dto.InterestResponseDTO;
import com.depositcore.entity.InterestAccrual;
import com.depositcore.entity.InterestPosting;
import com.depositcore.entity.PostingType;
import com.depositcore.repository.InterestAccrualRepository;
import com.depositcore.repository.InterestPostingRepository;
import com.depositcore.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interest")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService              interestService;
    private final InterestAccrualRepository    accrualRepo;
    private final InterestPostingRepository    postingRepo;

    /** Manual accrual — for testing or manual override */
    @PostMapping("/accrue")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<InterestResponseDTO> accrue(@Valid @RequestBody InterestRequestDTO request) {
        return ResponseEntity.ok(interestService.createAccrual(request));
    }

    /** Post all pending accruals for an account */
    @PostMapping("/post")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<InterestResponseDTO> post(
            @RequestParam Long accountId,
            @RequestParam PostingType postingType) {
        return ResponseEntity.ok(interestService.postInterest(accountId, postingType));
    }

    /** Get all accruals (for operations dashboard) */
    @GetMapping("/accruals")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<InterestAccrual>> getAllAccruals() {
        return ResponseEntity.ok(accrualRepo.findAll());
    }

    /** Get accruals for a specific account */
    @GetMapping("/accruals/account/{accountId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<InterestAccrual>> getAccrualsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accrualRepo.findByAccountId(accountId));
    }

    /** Get all postings */
    @GetMapping("/postings")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<InterestPosting>> getAllPostings() {
        return ResponseEntity.ok(postingRepo.findAll());
    }

    /** Get postings for a specific account */
    @GetMapping("/postings/account/{accountId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<InterestPosting>> getPostingsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(postingRepo.findByAccountId(accountId));
    }
}
