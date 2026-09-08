package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsTdsCertResponse(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("FinancialYear") String financialYear,
        @JsonProperty("TDSDeducted") BigDecimal tdsDeducted,
        @JsonProperty("Currency") String currency
) {}
