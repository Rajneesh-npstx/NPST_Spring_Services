package com.bank.td.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CloseDepositRequest(
        @NotBlank(message = "depositAccountNumber is required")
        String depositAccountNumber,

        @NotBlank(message = "destinationAccountNumber is required")
        String destinationAccountNumber,

        String remarks
) {}
