package com.bank.loan.entity;

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
        name = "disbursement_tranche",
        indexes = {
                @Index(name = "idx_tranche_loan_acc", columnList = "loan_account_number")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementTranche {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "loan_account_number", nullable = false, length = 40)
    private String loanAccountNumber;

    @Column(name = "tranche_number", nullable = false)
    private Integer trancheNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    @Column(nullable = false, length = 20)
    private String status; // SCHEDULED, DISBURSED

    @Column(name = "pre_emi_interest", precision = 18, scale = 2)
    private BigDecimal preEmiInterest;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
