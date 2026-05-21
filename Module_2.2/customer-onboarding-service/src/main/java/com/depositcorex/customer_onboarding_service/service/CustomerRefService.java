package com.depositcorex.customer_onboarding_service.service;

import com.depositcorex.customer_onboarding_service.model.CustomerRef;
import com.depositcorex.customer_onboarding_service.repository.CustomerRefRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomerRefService {

    private final CustomerRefRepository repository;

    public CustomerRefService(CustomerRefRepository repository) {
        this.repository = repository;
    }

    // Sync a new customer — throws if CIF already exists
    public CustomerRef saveCustomer(CustomerRef customer) {
        repository.findByCifNumber(customer.getCifNumber()).ifPresent(c -> {
            throw new IllegalStateException("Customer with CIF " + customer.getCifNumber() + " already exists.");
        });
        return repository.save(customer);
    }

    // Plain save — used for updates like linking userId
    public CustomerRef updateCustomer(CustomerRef customer) {
        return repository.save(customer);
    }

    public List<CustomerRef> getAllCustomers() {
        return repository.findAll();
    }

    public CustomerRef getCustomerByCif(String cifNumber) {
        return repository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer Reference with CIF " + cifNumber + " not found."));
    }

    public CustomerRef getCustomerByUserId(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No customer linked to user ID: " + userId));
    }

    public CustomerRef getCustomerByCustomerId(Long customerId) {
        return repository.findByCustomerID(customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No customer found with ID: " + customerId));
    }

    @CacheEvict(value = "customers", key = "#cifNumber")
    @Transactional
    public CustomerRef updateCustomerStatuses(String cifNumber, String kycStatus, String status) {
        CustomerRef customer = repository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer with CIF " + cifNumber + " not found."));
        if (kycStatus != null && !kycStatus.isBlank()) customer.setKycStatus(kycStatus);
        if (status != null && !status.isBlank()) customer.setStatus(status);
        return repository.save(customer);
    }
}
