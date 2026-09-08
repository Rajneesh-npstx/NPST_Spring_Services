package com.bank.loan.dto.response;

import com.bank.loan.enums.InterestType;
import com.bank.loan.enums.LoanStatus;
import com.bank.loan.enums.LoanType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LoanAccountResponse(
        UUID id,
        String loanAccountNumber,
        String customerId,
        UUID keycloakUserId,
        LoanType loanType,
        BigDecimal sanctionedAmount,
        BigDecimal disbursedAmount,
        BigDecimal outstandingPrincipal,
        BigDecimal interestRate,
        Integer tenureMonths,
        Integer remainingTenureMonths,
        BigDecimal emiAmount,
        LocalDate nextDueDate,
        LoanStatus status,
        InterestType interestType,
        String branchCode,
        String linkedDebitAccount,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt
) {}
