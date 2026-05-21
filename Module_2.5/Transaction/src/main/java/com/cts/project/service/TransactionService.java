package com.cts.project.service;

import java.util.List;

import com.cts.project.dto.TransactionRequestDTO;
import com.cts.project.dto.TransactionResponseDTO;

public interface TransactionService {
    TransactionResponseDTO postTransaction(TransactionRequestDTO request);
    TransactionResponseDTO reverseTransaction(Long txnId);
    TransactionResponseDTO getById(Long txnId);
    List<TransactionResponseDTO> getByAccountId(Long accountId);
    List<TransactionResponseDTO> getAll();
}
