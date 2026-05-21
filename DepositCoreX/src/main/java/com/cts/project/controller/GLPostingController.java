package com.cts.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.project.dto.GLPostingResponseDTO;
import com.cts.project.service.GLPostingService;

@RestController
@RequestMapping("/api/gl-postings")
public class GLPostingController {

    @Autowired
    private GLPostingService glPostingService;

    // Get all GL postings for a specific transaction
    @GetMapping("/transaction/{txnId}")
    public ResponseEntity<List<GLPostingResponseDTO>> getByTxnId(@PathVariable Long txnId) {
        return ResponseEntity.ok(glPostingService.getByTxnId(txnId));
    }

    // Get GL posting by ID
    @GetMapping("/{id}")
    public ResponseEntity<GLPostingResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(glPostingService.getById(id));
    }

    // Get all GL postings
    @GetMapping
    public ResponseEntity<List<GLPostingResponseDTO>> getAll() {
        return ResponseEntity.ok(glPostingService.getAll());
    }
}
