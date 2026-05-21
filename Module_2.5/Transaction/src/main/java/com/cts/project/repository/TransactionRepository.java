package com.cts.project.repository;

import com.cts.project.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdOrderByTxnDateDesc(Long accountId);
    Optional<Transaction> findTopByAccountIdAndStatusOrderByTxnDateDesc(Long accountId, String status);
}
