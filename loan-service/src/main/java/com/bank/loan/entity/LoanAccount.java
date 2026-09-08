package com.bank.loan.entity;

import com.bank.loan.enums.InterestType;
import com.bank.loan.enums.LoanStatus;
import com.bank.loan.enums.LoanType;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "loan_account",
        indexes = {
                @Index(name = "idx_loan_account_number", columnList = "loan_account_number", unique = true),
                @Index(name = "idx_loan_customer_id", columnList = "customer_id"),
                @Index(name = "idx_loan_status", columnList = "status"),
                @Index(name = "idx_loan_idempotency_key", columnList = "idempotency_key", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "loan_account_number", nullable = false, unique = true, length = 40)
    private String loanAccountNumber;

    @Column(name = "customer_id", nullable = false, length = 30)
    private String customerId; // CIF

    @Column(name = "keycloak_user_id")
    private UUID keycloakUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 30)
    private LoanType loanType;

    @Column(name = "sanctioned_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal sanctionedAmount;

    @Column(name = "disbursed_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(name = "outstanding_principal", nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Column(name = "interest_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "remaining_tenure_months", nullable = false)
    private Integer remainingTenureMonths;

    @Column(name = "emi_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 20)
    @Builder.Default
    private InterestType interestType = InterestType.FLOATING;

    @Column(name = "branch_code", length = 20)
    private String branchCode;

    @Column(name = "linked_debit_account", length = 40)
    private String linkedDebitAccount;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
