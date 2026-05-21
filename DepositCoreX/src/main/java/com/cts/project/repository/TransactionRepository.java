
package com.cts.project.repository;

import com.cts.project.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {}