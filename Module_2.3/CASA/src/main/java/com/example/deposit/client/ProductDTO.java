package com.example.deposit.client;

import lombok.Data;

@Data
public class ProductDTO {
    private Long productID;
    private String productName;
    private String category;
    private Double minAmount;
    private Double maxAmount;
    private Integer minTenure;
    private Integer maxTenure;
    private String interestMethod;
    private String status;
}
