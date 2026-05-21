package com.depositcore.repository;

import com.depositcore.entity.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterestAccrualRepository extends JpaRepository<InterestAccrual, Long> {

    List<InterestAccrual> findByAccountIdAndStatus(Long accountId, String status);
    List<InterestAccrual> findByAccountId(Long accountId);
    InterestAccrual findTopByAccountIdOrderByPeriodEndDesc(Long accountId);

    boolean existsByAccountIdAndPeriodStartLessThanAndPeriodEndGreaterThan(
            Long accountId, LocalDate periodEnd, LocalDate periodStart);

    @Query("SELECT DISTINCT a.accountId FROM InterestAccrual a WHERE a.status = 'PENDING'")
    List<Long> findAccountIdsWithPendingAccruals();

    @Query("SELECT DISTINCT a.accountId FROM InterestAccrual a")
    List<Long> findDistinctAccountIds();

    default List<InterestAccrual> findByAccountIdAndPostedFalse(Long accountId) {
        return findByAccountIdAndStatus(accountId, "PENDING");
    }
}
