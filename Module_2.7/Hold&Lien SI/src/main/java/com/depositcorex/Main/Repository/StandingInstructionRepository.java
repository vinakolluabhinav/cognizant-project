package com.depositcorex.Main.Repository;

import com.depositcorex.Main.Entities.StandingInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StandingInstructionRepository extends JpaRepository<StandingInstruction, Long> {

    List<StandingInstruction> findByStatusAndNextRunDateLessThanEqual(String status, LocalDate date);

    List<StandingInstruction> findByFromAccountId(Long fromAccountId);
}
