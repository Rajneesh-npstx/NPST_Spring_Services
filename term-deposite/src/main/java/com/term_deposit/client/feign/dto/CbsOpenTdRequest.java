package com.term_deposit.client.feign.dto;

import java.math.BigDecimal;

public record CbsOpenTdRequest(
        String cif,
        String productCode,
        BigDecimal principal,
        Integer tenureMonths,
        String fundingAccountNumber,
        String interestPayoutMode
) {}