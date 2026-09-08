package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsTdsCertRequest(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("FinancialYear") String financialYear
) {}
