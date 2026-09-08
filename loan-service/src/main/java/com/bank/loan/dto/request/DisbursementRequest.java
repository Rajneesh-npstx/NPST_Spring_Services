package com.bank.loan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DisbursementRequest(
        @NotNull(message = "trancheNumber is required")
        @Positive(message = "trancheNumber must be positive")
        Integer trancheNumber,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        LocalDate disbursementDate
) {}
