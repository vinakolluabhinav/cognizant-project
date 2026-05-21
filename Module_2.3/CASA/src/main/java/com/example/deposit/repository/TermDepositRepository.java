package com.example.deposit.repository;

import com.example.deposit.entity.TermDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TermDepositRepository extends JpaRepository<TermDeposit, Long> {
    Optional<TermDeposit> findByAccountId(Long accountId);
    List<TermDeposit> findByStatus(String status);
    List<TermDeposit> findByMaturityDateAndStatus(LocalDate maturityDate, String status);
    Optional<TermDeposit> findByTdId(Long tdId);
}
