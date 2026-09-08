package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ForeclosureStatementResponse(
        String statementNumber,
        String loanAccountNumber,
        String customerId,
        LocalDate settlementDate,
        BigDecimal outstandingPrincipal,
        BigDecimal proRataInterest,
        BigDecimal foreclosureCharges,
        BigDecimal totalSettlementAmount,
        boolean chargeWaiverApplied,
        String waiverNotes
) {}
