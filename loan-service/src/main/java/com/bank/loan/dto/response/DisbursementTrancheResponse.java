package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DisbursementTrancheResponse(
        Integer trancheNumber,
        BigDecimal amount,
        LocalDate disbursementDate,
        String status,
        BigDecimal preEmiInterest
) {}
