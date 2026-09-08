package com.bank.loan.dto.response;

import com.bank.loan.enums.LoanStatus;
import com.bank.loan.enums.LoanType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApplicationResponse(
        UUID id,
        String applicationNumber,
        String customerId,
        LoanType loanType,
        BigDecimal requestedAmount,
        Integer tenureMonths,
        BigDecimal monthlyIncome,
        String branchCode,
        LoanStatus status,
        BigDecimal estimatedEmi,
        String message,
        Instant createdAt
) {}
