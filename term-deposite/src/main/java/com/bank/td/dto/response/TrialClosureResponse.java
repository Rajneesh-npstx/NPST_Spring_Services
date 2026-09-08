package com.bank.td.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrialClosureResponse(
        String depositAccountNumber,
        BigDecimal principalAmount,
        LocalDate depositDate,
        LocalDate simulationDate,
        long daysHeld,
        BigDecimal contractedRate,
        BigDecimal applicableRate,
        BigDecimal grossPayable,
        BigDecimal penaltyAmount,
        BigDecimal excessInterestRecovered,
        BigDecimal netPayable,
        boolean hasActiveLien,
        String message
) {}
