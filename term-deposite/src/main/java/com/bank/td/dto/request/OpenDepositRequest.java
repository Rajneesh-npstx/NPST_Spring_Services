package com.bank.td.dto.request;

import com.bank.td.enums.AutoRenewalType;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenDepositRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        UUID keycloakUserId,

        @NotBlank(message = "debitAccountNumber is required")
        String debitAccountNumber,

        @NotNull(message = "depositType is required")
        DepositType depositType,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotNull(message = "tenureMonths is required")
        @Positive(message = "tenureMonths must be positive")
        Integer tenureMonths,

        InterestPayout interestPayout,

        AutoRenewalType autoRenewal,

        String nomineeName,

        String nomineeRelationship,

        Boolean nomineeMinor,

        String idempotencyKey
) {}
