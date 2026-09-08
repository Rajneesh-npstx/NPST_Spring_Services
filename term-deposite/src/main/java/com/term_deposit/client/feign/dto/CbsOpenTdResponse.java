package com.term_deposit.client.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CbsOpenTdResponse(
        String cbsReferenceNumber,
        String status,
        BigDecimal appliedInterestRate,
        LocalDate maturityDate,
        BigDecimal maturityAmount
) {}