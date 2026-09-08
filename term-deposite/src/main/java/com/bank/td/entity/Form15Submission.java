package com.bank.td.entity;

import com.bank.td.enums.FormStatus;
import com.bank.td.enums.FormType;
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
import java.util.UUID;

@Entity
@Table(
        name = "form_15_submission",
        indexes = {
                @Index(name = "idx_form15_customer_id", columnList = "customer_id"),
                @Index(name = "idx_form15_pan", columnList = "pan")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form15Submission {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 30)
    private String customerId;

    @Column(nullable = false, length = 10)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 20)
    private FormType formType;

    @Column(name = "customer_age", nullable = false)
    private Integer customerAge;

    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;

    @Column(name = "estimated_total_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal estimatedTotalIncome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormStatus status;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @CreatedDate
    @Column(name = "submitted_at", updatable = false)
    private Instant submittedAt;
}
