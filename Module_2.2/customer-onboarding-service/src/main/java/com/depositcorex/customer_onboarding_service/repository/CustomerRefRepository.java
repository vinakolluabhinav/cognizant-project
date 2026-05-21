package com.depositcorex.customer_onboarding_service.repository;

import com.depositcorex.customer_onboarding_service.model.CustomerRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRefRepository extends JpaRepository<CustomerRef, Long> {
    Optional<CustomerRef> findByCifNumber(String cifNumber);
    Optional<CustomerRef> findByUserId(Long userId);
    Optional<CustomerRef> findByCustomerID(Long customerID);
}
