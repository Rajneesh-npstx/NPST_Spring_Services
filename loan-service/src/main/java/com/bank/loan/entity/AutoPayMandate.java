package com.bank.loan.entity;

import com.bank.loan.enums.MandateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "autopay_mandate",
        indexes = {
                @Index(name = "idx_mandate_loan_acc", columnList = "loan_account_number"),
                @Index(name = "idx_mandate_ref", columnList = "mandate_reference", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoPayMandate {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "mandate_reference", nullable = false, unique = true, length = 60)
    private String mandateReference;

    @Column(name = "loan_account_number", nullable = false, length = 40)
    private String loanAccountNumber;

    @Column(name = "customer_id", nullable = false, length = 30)
    private String customerId;

    @Column(name = "debit_account_number", nullable = false, length = 40)
    private String debitAccountNumber;

    @Column(name = "debit_day_of_month", nullable = false)
    private Integer debitDayOfMonth;

    @Column(name = "max_debit_amount", precision = 18, scale = 2)
    private BigDecimal maxDebitAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MandateStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
