package com.depositcorex.customer_onboarding_service.controller;

import com.depositcorex.customer_onboarding_service.model.CustomerRef;
import com.depositcorex.customer_onboarding_service.service.CustomerRefService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer-reference")
@RequiredArgsConstructor
public class CustomerRefController {

    private final CustomerRefService service;

    @Transactional
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> syncCustomer(@Valid @RequestBody CustomerRef customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCustomer(customer));
    }

    @GetMapping("/view")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<List<CustomerRef>> getAll() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @GetMapping("/{cifNumber}")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> getByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(service.getCustomerByCif(cifNumber));
    }

    @PatchMapping("/{cifNumber}")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> updateStatuses(
            @PathVariable String cifNumber,
            @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(service.updateCustomerStatuses(
                cifNumber,
                updates.get("kycStatus"),
                updates.get("status")));
    }

    @GetMapping("/by-userid/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getCustomerByUserId(userId));
    }

    @GetMapping("/by-customerid/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getCustomerByCustomerId(customerId));
    }

    @PatchMapping("/{cifNumber}/link-user")
    @PreAuthorize("hasAnyRole('BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'CORE_ADMIN')")
    public ResponseEntity<CustomerRef> linkUser(
            @PathVariable String cifNumber,
            @RequestParam Long userId) {
        CustomerRef c = service.getCustomerByCif(cifNumber);
        c.setUserId(userId);
        return ResponseEntity.ok(service.updateCustomer(c));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Service is UP";
    }
}
