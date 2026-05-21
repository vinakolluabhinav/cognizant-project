package com.depositcorex.tdservicing.repository;

import com.depositcorex.tdservicing.entity.PrematureClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrematureClosureRepository extends JpaRepository<PrematureClosure, Long> {
    List<PrematureClosure> findByTdId(Long tdId);
    Optional<PrematureClosure> findFirstByTdId(Long tdId);
}
