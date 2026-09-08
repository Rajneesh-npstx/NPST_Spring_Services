package com.bank.td.dto.request;

import com.bank.td.enums.FormType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record Form15Request(
        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "pan is required")
        String pan,

        @NotNull(message = "formType is required")
        FormType formType,

        @NotNull(message = "customerAge is required")
        @Positive(message = "customerAge must be positive")
        Integer customerAge,

        @NotBlank(message = "financialYear is required")
        String financialYear,

        @NotNull(message = "estimatedTotalIncome is required")
        BigDecimal estimatedTotalIncome
) {}
