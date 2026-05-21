package com.example.deposit.dto;

import java.math.BigDecimal;

public class TermDepositRequest {

    private String cifNumber;
    private Long productId;
    private String currency;
    private BigDecimal principalAmount;
    private Integer tenureMonths;
    private String payoutMode;
    private BigDecimal rate;

    public String getCifNumber() { return cifNumber; }
    public void setCifNumber(String cifNumber) { this.cifNumber = cifNumber; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public String getPayoutMode() { return payoutMode; }
    public void setPayoutMode(String payoutMode) { this.payoutMode = payoutMode; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}
