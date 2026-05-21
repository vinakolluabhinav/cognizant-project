package com.example.deposit.repository;

import com.example.deposit.entity.DepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {
    Optional<DepositAccount> findByAccountNumber(String accountNumber);
    List<DepositAccount> findByCustomerId(Long customerId);
    List<DepositAccount> findByStatusAndCategoryIn(String status, List<String> categories);
    List<DepositAccount> findByStatus(String status);
}
