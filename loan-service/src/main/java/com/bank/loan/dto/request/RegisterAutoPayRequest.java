package com.bank.loan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterAutoPayRequest(
        @NotBlank(message = "debitAccountNumber is required")
        String debitAccountNumber,

        @NotNull(message = "debitDayOfMonth is required")
        @Min(value = 1, message = "debitDayOfMonth must be between 1 and 28")
        @Max(value = 28, message = "debitDayOfMonth must be between 1 and 28")
        Integer debitDayOfMonth,

        BigDecimal maxDebitAmount
) {}
