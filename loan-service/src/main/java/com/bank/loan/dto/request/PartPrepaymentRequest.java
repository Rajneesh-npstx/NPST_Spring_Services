package com.bank.loan.dto.request;

import com.bank.loan.enums.PrepaymentAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PartPrepaymentRequest(
        @NotNull(message = "prepaymentAmount is required")
        @Positive(message = "prepaymentAmount must be positive")
        BigDecimal prepaymentAmount,

        @NotNull(message = "action is required")
        PrepaymentAction action,

        @NotBlank(message = "debitAccountNumber is required")
        String debitAccountNumber
) {}
