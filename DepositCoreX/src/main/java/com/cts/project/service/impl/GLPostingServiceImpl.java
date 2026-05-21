package com.cts.project.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.project.dto.GLPostingResponseDTO;
import com.cts.project.entity.GLPosting;
import com.cts.project.repository.GLPostingRepository;
import com.cts.project.service.GLPostingService;

@Service
public class GLPostingServiceImpl implements GLPostingService {

    @Autowired
    private GLPostingRepository glPostingRepository;

    @Override
    public List<GLPostingResponseDTO> getByTxnId(Long txnId) {
        return glPostingRepository.findAll().stream()
                .filter(gl -> gl.getTxn() != null && gl.getTxn().getTxnId().equals(txnId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GLPostingResponseDTO getById(Long glPostingId) {
        GLPosting gl = glPostingRepository.findById(glPostingId)
                .orElseThrow(() -> new RuntimeException("GL Posting not found: " + glPostingId));
        return mapToResponse(gl);
    }

    @Override
    public List<GLPostingResponseDTO> getAll() {
        return glPostingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private GLPostingResponseDTO mapToResponse(GLPosting gl) {
        GLPostingResponseDTO dto = new GLPostingResponseDTO();
        dto.setGlPostingId(gl.getGlPostingId());
        dto.setTxnId(gl.getTxn() != null ? gl.getTxn().getTxnId() : null);
        dto.setGlAccount(gl.getGlAccount());
        dto.setDebitOrCredit(gl.getDebitOrCredit());
        dto.setAmount(gl.getAmount());
        dto.setPostedDate(gl.getPostedDate());
        return dto;
    }
}
