package com.bank.loan.dto.response;

import com.bank.loan.enums.MandateStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AutoPayResponse(
        UUID id,
        String mandateReference,
        String loanAccountNumber,
        String customerId,
        String debitAccountNumber,
        Integer debitDayOfMonth,
        BigDecimal maxDebitAmount,
        MandateStatus status,
        Instant createdAt
) {}
