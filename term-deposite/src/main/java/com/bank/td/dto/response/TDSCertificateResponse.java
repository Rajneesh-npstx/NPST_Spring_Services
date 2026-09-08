package com.bank.td.dto.response;

import java.math.BigDecimal;

public record TDSCertificateResponse(
        String certificateNumber,
        String customerId,
        String financialYear,
        BigDecimal totalTdsDeducted,
        String currency,
        String source
) {}
