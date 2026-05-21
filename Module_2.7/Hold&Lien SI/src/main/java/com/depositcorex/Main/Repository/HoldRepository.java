package com.depositcorex.Main.Repository;

import com.depositcorex.Main.Entities.HoldOrLien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HoldRepository extends JpaRepository<HoldOrLien, Long> {

    List<HoldOrLien> findByAccountIdAndStatus(Long accountId, String status);

    @Query("SELECT SUM(h.amount) FROM HoldOrLien h WHERE h.accountId = :accountId AND h.status = 'ACTIVE'")
    BigDecimal sumActiveHolds(@Param("accountId") Long accountId);
}
