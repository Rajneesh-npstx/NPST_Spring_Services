package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AmortizationScheduleResponse(
        String loanAccountNumber,
        BigDecimal sanctionedAmount,
        BigDecimal outstandingPrincipal,
        BigDecimal interestRate,
        Integer totalTenureMonths,
        Integer remainingTenureMonths,
        BigDecimal monthlyEmi,
        String source,
        List<ScheduleItemResponse> schedule
) {}
