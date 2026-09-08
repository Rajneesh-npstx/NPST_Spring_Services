package com.bank.td.dto.response;

import com.bank.td.enums.AutoRenewalType;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositAdviceResponse(
        String adviceNumber,
        String depositAccountNumber,
        String customerId,
        DepositType depositType,
        InterestPayout interestPayout,
        BigDecimal principalAmount,
        BigDecimal interestRate,
        BigDecimal maturityAmount,
        LocalDate depositDate,
        LocalDate maturityDate,
        Integer tenureMonths,
        AutoRenewalType autoRenewal,
        String nomineeName,
        String nomineeRelationship,
        String issuingBranch,
        String termsAndConditionsSummary
) {}
