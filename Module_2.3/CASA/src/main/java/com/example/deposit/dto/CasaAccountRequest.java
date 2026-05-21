package com.example.deposit.dto;

public class CasaAccountRequest {

    private String cifNumber;
    private Long productId;
    private String category;
    private String currency;

    public String getCifNumber() { return cifNumber; }
    public void setCifNumber(String cifNumber) { this.cifNumber = cifNumber; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
