package com.cts.project.repository;

import com.cts.project.entity.GLPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GLPostingRepository extends JpaRepository<GLPosting, Long> {
    List<GLPosting> findByTxn_TxnIdOrderByPostedDateDesc(Long txnId);
}
