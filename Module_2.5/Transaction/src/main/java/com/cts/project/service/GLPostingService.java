package com.cts.project.service;

import java.util.List;

import com.cts.project.dto.GLPostingResponseDTO;

public interface GLPostingService {
    List<GLPostingResponseDTO> getAll();
    List<GLPostingResponseDTO> getByTxnId(Long txnId);
    GLPostingResponseDTO getById(Long glPostingId);
}
