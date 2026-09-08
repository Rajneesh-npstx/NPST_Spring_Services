package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsInterestCertResponse(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("FinancialYear") String financialYear,
        @JsonProperty("InterestEarned") BigDecimal interestEarned,
        @JsonProperty("Currency") String currency
) {}
