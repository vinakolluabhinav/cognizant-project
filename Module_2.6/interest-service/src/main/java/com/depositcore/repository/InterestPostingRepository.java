package com.depositcore.repository;

import com.depositcore.entity.InterestPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestPostingRepository extends JpaRepository<InterestPosting, Long> {
    boolean existsByAccountIdAndAccrualId(Long accountId, Long accrualId);
    List<InterestPosting> findByAccountId(Long accountId);
}
