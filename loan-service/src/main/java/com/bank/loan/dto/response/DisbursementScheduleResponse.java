package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DisbursementScheduleResponse(
        String loanAccountNumber,
        BigDecimal sanctionedAmount,
        BigDecimal totalDisbursedAmount,
        BigDecimal pendingDisbursementAmount,
        List<DisbursementTrancheResponse> tranches
) {}
