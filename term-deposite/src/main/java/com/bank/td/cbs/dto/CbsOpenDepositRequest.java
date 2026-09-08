package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsOpenDepositRequest(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("DebitAccountNumber") String debitAccountNumber,
        @JsonProperty("DepositType") String depositType,
        @JsonProperty("Amount") BigDecimal amount,
        @JsonProperty("TenureMonths") Integer tenureMonths,
        @JsonProperty("InterestPayout") String interestPayout,
        @JsonProperty("AutoRenewal") Boolean autoRenewal
) {}
