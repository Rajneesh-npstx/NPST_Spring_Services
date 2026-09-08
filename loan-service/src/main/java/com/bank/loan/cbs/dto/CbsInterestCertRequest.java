package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsInterestCertRequest(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("FinancialYear") String financialYear
) {}
