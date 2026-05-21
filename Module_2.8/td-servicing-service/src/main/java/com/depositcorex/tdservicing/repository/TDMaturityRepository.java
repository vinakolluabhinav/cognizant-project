package com.depositcorex.tdservicing.repository;

import com.depositcorex.tdservicing.entity.TDMaturity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TDMaturityRepository extends JpaRepository<TDMaturity, Long> {
    List<TDMaturity> findByTdId(Long tdId);
    Optional<TDMaturity> findFirstByTdId(Long tdId);
    List<TDMaturity> findByStatus(String status);
}
