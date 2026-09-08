package com.bank.td.dto.response;

import java.math.BigDecimal;

public record InterestCertificateResponse(
        String certificateNumber,
        String customerId,
        String financialYear,
        BigDecimal totalInterestEarned,
        String currency,
        String source
) {}
