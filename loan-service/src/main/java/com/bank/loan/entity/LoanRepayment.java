package com.bank.loan.entity;

import com.bank.loan.enums.RepaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "loan_repayment",
        indexes = {
                @Index(name = "idx_repay_loan_acc", columnList = "loan_account_number"),
                @Index(name = "idx_repay_ref", columnList = "payment_reference", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepayment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 60)
    private String paymentReference;

    @Column(name = "loan_account_number", nullable = false, length = 40)
    private String loanAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private RepaymentType paymentType;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "principal_component", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalComponent;

    @Column(name = "interest_component", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestComponent;

    @Column(name = "penal_charges", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal penalCharges = BigDecimal.ZERO;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
