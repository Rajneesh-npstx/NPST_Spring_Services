package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsOpenDepositResponse(
        @JsonProperty("DepositAccountNumber") String depositAccountNumber,
        @JsonProperty("PrincipalAmount") BigDecimal principalAmount,
        @JsonProperty("InterestRate") BigDecimal interestRate,
        @JsonProperty("MaturityAmount") BigDecimal maturityAmount,
        @JsonProperty("MaturityDate") String maturityDate,
        @JsonProperty("Status") String status,
        @JsonProperty("Message") String message
) {}
