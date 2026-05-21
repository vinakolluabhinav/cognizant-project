package com.cts.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "glposting")
public class GLPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "glpostingid")
    private Long glPostingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txnid")
    private Transaction txn;

    @Column(name = "glaccount", length = 60)
    private String glAccount;

    @Column(name = "debitorCredit", length = 20, columnDefinition = "varchar(20)")
    private String debitOrCredit;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "posteddate")
    private LocalDateTime postedDate;

    public GLPosting() {}

    public GLPosting(Long glPostingId, Transaction txn, String glAccount,
                     String debitOrCredit, BigDecimal amount, LocalDateTime postedDate) {
        this.glPostingId = glPostingId;
        this.txn = txn;
        this.glAccount = glAccount;
        this.debitOrCredit = debitOrCredit;
        this.amount = amount;
        this.postedDate = postedDate;
    }

    public Long getGlPostingId() { return glPostingId; }
    public void setGlPostingId(Long glPostingId) { this.glPostingId = glPostingId; }

    public Transaction getTxn() { return txn; }
    public void setTxn(Transaction txn) { this.txn = txn; }

    public String getGlAccount() { return glAccount; }
    public void setGlAccount(String glAccount) { this.glAccount = glAccount; }

    public String getDebitOrCredit() { return debitOrCredit; }
    public void setDebitOrCredit(String debitOrCredit) { this.debitOrCredit = debitOrCredit; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }

    public static GLPostingBuilder builder() { return new GLPostingBuilder(); }

    public static class GLPostingBuilder {
        private Long glPostingId;
        private Transaction txn;
        private String glAccount;
        private String debitOrCredit;
        private BigDecimal amount;
        private LocalDateTime postedDate;

        public GLPostingBuilder glPostingId(Long glPostingId) { this.glPostingId = glPostingId; return this; }
        public GLPostingBuilder txn(Transaction txn) { this.txn = txn; return this; }
        public GLPostingBuilder glAccount(String glAccount) { this.glAccount = glAccount; return this; }
        public GLPostingBuilder debitOrCredit(String debitOrCredit) { this.debitOrCredit = debitOrCredit; return this; }
        public GLPostingBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public GLPostingBuilder postedDate(LocalDateTime postedDate) { this.postedDate = postedDate; return this; }

        public GLPosting build() {
            return new GLPosting(glPostingId, txn, glAccount, debitOrCredit, amount, postedDate);
        }
    }
}
