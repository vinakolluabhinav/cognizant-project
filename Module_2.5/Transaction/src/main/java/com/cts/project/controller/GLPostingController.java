package com.cts.project.controller;

import com.cts.project.dto.GLPostingResponseDTO;
import com.cts.project.service.GLPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gl-postings")
@RequiredArgsConstructor
public class GLPostingController {

    private final GLPostingService glPostingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<GLPostingResponseDTO>> getAll() {
        return ResponseEntity.ok(glPostingService.getAll());
    }

    @GetMapping("/transaction/{txnId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<GLPostingResponseDTO>> getByTransaction(@PathVariable Long txnId) {
        return ResponseEntity.ok(glPostingService.getByTxnId(txnId));
    }

    @GetMapping("/{glId}")
    @PreAuthorize("hasAnyRole('OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<GLPostingResponseDTO> getById(@PathVariable Long glId) {
        return ResponseEntity.ok(glPostingService.getById(glId));
    }
}
