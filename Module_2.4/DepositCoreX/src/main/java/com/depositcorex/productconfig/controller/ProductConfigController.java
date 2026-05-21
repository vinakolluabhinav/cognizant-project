package com.depositcorex.productconfig.controller;

import com.depositcorex.productconfig.dto.SimulationResponse;
import com.depositcorex.productconfig.entity.DepositProduct;
import com.depositcorex.productconfig.service.ProductConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductConfigController {

    @Autowired
    private ProductConfigService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('FINANCE_ANALYST', 'CORE_ADMIN')")
    public DepositProduct createProduct(@RequestBody DepositProduct product) {
        return service.saveProduct(product);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public List<DepositProduct> getAll() {
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public DepositProduct getProduct(@PathVariable Long id) {
        return service.getProductById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCE_ANALYST', 'CORE_ADMIN')")
    public DepositProduct updateProduct(@PathVariable Long id, @RequestBody DepositProduct product) {
        product.setProductID(id);
        return service.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CORE_ADMIN')")
    public String deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return "Product " + id + " has been successfully deleted.";
    }

    @GetMapping("/{id}/simulate")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BRANCH_OFFICER', 'OPERATIONS_OFFICER', 'FINANCE_ANALYST', 'CORE_ADMIN')")
    public SimulationResponse simulate(@PathVariable Long id,
                                       @RequestParam Double amount,
                                       @RequestParam Integer tenure) {
        return service.simulateMaturity(id, amount, tenure);
    }

}
