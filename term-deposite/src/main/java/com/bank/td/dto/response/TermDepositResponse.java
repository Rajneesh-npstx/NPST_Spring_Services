package com.bank.td.dto.response;

import com.bank.td.enums.AutoRenewalType;
import com.bank.td.enums.DepositStatus;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TermDepositResponse(
        UUID id,
        String depositAccountNumber,
        String customerId,
        UUID keycloakUserId,
        DepositType depositType,
        InterestPayout interestPayout,
        BigDecimal principalAmount,
        BigDecimal interestRate,
        BigDecimal maturityAmount,
        LocalDate depositDate,
        LocalDate maturityDate,
        Integer tenureMonths,
        String debitAccountNumber,
        AutoRenewalType autoRenewal,
        DepositStatus status,
        String nomineeName,
        String nomineeRelationship,
        Boolean nomineeMinor,
        Boolean hasLien,
        String lienReason,
        Instant createdAt,
        Instant updatedAt
) {}
