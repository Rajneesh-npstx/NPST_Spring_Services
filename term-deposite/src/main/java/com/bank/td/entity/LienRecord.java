package com.bank.td.entity;

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
        name = "lien_record",
        indexes = {
                @Index(name = "idx_lien_account_number", columnList = "deposit_account_number"),
                @Index(name = "idx_lien_active", columnList = "active")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LienRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "deposit_account_number", nullable = false, length = 40)
    private String depositAccountNumber;

    @Column(name = "lien_reference", nullable = false, unique = true, length = 60)
    private String lienReference;

    @Column(name = "lien_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal lienAmount;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(name = "marked_at", updatable = false)
    private Instant markedAt;

    @Column(name = "released_at")
    private Instant releasedAt;
}
