package com.depositcore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "interest_accrual",
    uniqueConstraints = {
        // L13 — Prevent duplicate accrual for same account and period
        @UniqueConstraint(
            name = "uk_accrual_account_period",
            columnNames = {"account_id", "period_start", "period_end"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accrualId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    // L15 — Product reference for rate slab lookup and reporting
    @Column
    private Long productId;

    // Period
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // L3 — Store the basis used for calculation (auditable)
    @Column(nullable = false, precision = 19, scale = 4)  // T2 — correct precision
    private BigDecimal principal;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal rateApplied;                       // e.g. 5.5000

    @Column(nullable = false, length = 20)
    private String interestMethod;                        // SIMPLE / COMPOUNDED

    // T2 — Correct precision for monetary amount
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal interestAmount;

    // T3 — Currency denomination
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    // Backward-compatibility column — kept for existing DB schema
    // The 'status' field is the authoritative source; this is derived
    @Column(name = "posted", nullable = false)
    @Builder.Default
    private boolean posted = false;

    // L8 — Proper status field
    // Values: PENDING / POSTED / REVERSED
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    // Audit timestamps
    @Column(nullable = false)
    private LocalDateTime calculatedDate;

    @Column
    private LocalDateTime postedDate;

    // Keeps the 'posted' boolean column in sync when status changes
    public void setStatus(String status) {
        this.status = status;
        this.posted = "POSTED".equalsIgnoreCase(status);
    }

    public boolean isPosted() {
        return "POSTED".equalsIgnoreCase(this.status);
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
        this.status = posted ? "POSTED" : "PENDING";
    }
}
