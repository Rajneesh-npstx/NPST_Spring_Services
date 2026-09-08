package com.bank.loan.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ForeclosureRequest(
        LocalDate settlementDate,

        @NotBlank(message = "debitAccountNumber is required")
        String debitAccountNumber,

        String remarks
) {}
