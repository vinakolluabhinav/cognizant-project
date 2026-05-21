package com.cts.project.service.impl;

import com.cts.project.dto.GLPostingResponseDTO;
import com.cts.project.entity.GLPosting;
import com.cts.project.repository.GLPostingRepository;
import com.cts.project.service.GLPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GLPostingServiceImpl implements GLPostingService {

    private final GLPostingRepository glPostingRepository;

    @Override
    public List<GLPostingResponseDTO> getByTxnId(Long txnId) {
        return glPostingRepository.findByTxn_TxnIdOrderByPostedDateDesc(txnId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public GLPostingResponseDTO getById(Long glPostingId) {
        GLPosting gl = glPostingRepository.findById(glPostingId)
                .orElseThrow(() -> new RuntimeException("GL Posting not found: " + glPostingId));
        return toDTO(gl);
    }

    @Override
    public List<GLPostingResponseDTO> getAll() {
        return glPostingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private GLPostingResponseDTO toDTO(GLPosting gl) {
        return new GLPostingResponseDTO(
            gl.getGlPostingId(),
            gl.getTxn() != null ? gl.getTxn().getTxnId() : null,
            gl.getTxn() != null ? gl.getTxn().getAccountId() : null,
            gl.getGlAccount(),
            gl.getDebitOrCredit(),
            gl.getAmount(),
            gl.getPostedDate()
        );
    }
}
