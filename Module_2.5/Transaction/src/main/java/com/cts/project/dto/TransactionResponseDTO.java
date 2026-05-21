package com.cts.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    private Long txnId;
    private Long accountId;
    private String accountNumber;
    private String txnType;
    private BigDecimal amount;
    private String narrative;
    private String channel;
    private LocalDateTime txnDate;
    private BigDecimal balanceAfter;
    private String status;

    public Long getTxnId() { return txnId; }
    public void setTxnId(Long txnId) { this.txnId = txnId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

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

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
