package com.bank.loan.dto.request;

import com.bank.loan.enums.InterestType;
import com.bank.loan.enums.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyLoanRequest(
        @NotBlank(message = "customerId is required")
        String customerId,

        UUID keycloakUserId,

        @NotNull(message = "loanType is required")
        LoanType loanType,

        @NotNull(message = "requestedAmount is required")
        @Positive(message = "requestedAmount must be positive")
        BigDecimal requestedAmount,

        @NotNull(message = "tenureMonths is required")
        @Positive(message = "tenureMonths must be positive")
        Integer tenureMonths,

        @NotNull(message = "monthlyIncome is required")
        @Positive(message = "monthlyIncome must be positive")
        BigDecimal monthlyIncome,

        String branchCode,

        InterestType interestType,

        String idempotencyKey
) {}
