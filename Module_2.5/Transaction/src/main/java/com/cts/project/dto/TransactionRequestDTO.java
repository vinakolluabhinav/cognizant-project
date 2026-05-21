package com.cts.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRequestDTO {
    private Long accountId;
    private String txnType;
    private BigDecimal amount;
    private String narrative;
    private String channel;
    private LocalDateTime txnDate;
    private String status;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public LocalDateTime getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDateTime txnDate) { this.txnDate = txnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
