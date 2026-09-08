package com.bank.td.entity;

import com.bank.td.enums.AutoRenewalType;
import com.bank.td.enums.DepositStatus;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;
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
        name = "term_deposit",
        indexes = {
                @Index(name = "idx_td_customer_id", columnList = "customer_id"),
                @Index(name = "idx_td_account_number", columnList = "deposit_account_number", unique = true),
                @Index(name = "idx_td_status", columnList = "status"),
                @Index(name = "idx_td_idempotency_key", columnList = "idempotency_key", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermDeposit {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "deposit_account_number", nullable = false, unique = true, length = 40)
    private String depositAccountNumber;

    @Column(name = "customer_id", nullable = false, length = 30)
    private String customerId; // CIF

    @Column(name = "keycloak_user_id")
    private UUID keycloakUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", nullable = false, length = 30)
    private DepositType depositType;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_payout", nullable = false, length = 30)
    private InterestPayout interestPayout;

    @Column(name = "principal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "maturity_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maturityAmount;

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "debit_account_number", nullable = false, length = 40)
    private String debitAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_renewal", nullable = false, length = 30)
    private AutoRenewalType autoRenewal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DepositStatus status;

    @Column(name = "nominee_name", length = 100)
    private String nomineeName;

    @Column(name = "nominee_relationship", length = 50)
    private String nomineeRelationship;

    @Column(name = "nominee_minor")
    private Boolean nomineeMinor;

    @Column(name = "has_lien", nullable = false)
    @Builder.Default
    private Boolean hasLien = false;

    @Column(name = "lien_reason", length = 255)
    private String lienReason;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "settlement_account_number", length = 40)
    private String settlementAccountNumber;

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
