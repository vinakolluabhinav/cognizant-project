package com.example.deposit.service.validation;

import com.example.deposit.client.ProductDTO;
import com.example.deposit.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountValidator {

    public void validate(BigDecimal principalAmount, ProductDTO product) {
        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Principal amount must be greater than zero");
        }
        if (product.getMinAmount() != null &&
                principalAmount.compareTo(BigDecimal.valueOf(product.getMinAmount())) < 0) {
            throw new ValidationException("Principal amount is less than minimum allowed for this product");
        }
        if (product.getMaxAmount() != null &&
                principalAmount.compareTo(BigDecimal.valueOf(product.getMaxAmount())) > 0) {
            throw new ValidationException("Principal amount exceeds maximum allowed for this product");
        }
    }
}
