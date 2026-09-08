package com.bank.td.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MarkLienRequest(
        @NotNull(message = "lienAmount is required")
        @Positive(message = "lienAmount must be positive")
        BigDecimal lienAmount,

        @NotBlank(message = "reason is required")
        String reason
) {}
