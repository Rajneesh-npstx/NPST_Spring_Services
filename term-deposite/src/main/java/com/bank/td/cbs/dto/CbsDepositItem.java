package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsDepositItem(
        @JsonProperty("DepositAccountNumber") String depositAccountNumber,
        @JsonProperty("DepositType") String depositType,
        @JsonProperty("PrincipalAmount") BigDecimal principalAmount,
        @JsonProperty("InterestRate") BigDecimal interestRate,
        @JsonProperty("MaturityAmount") BigDecimal maturityAmount,
        @JsonProperty("DepositDate") String depositDate,
        @JsonProperty("MaturityDate") String maturityDate,
        @JsonProperty("InterestPayout") String interestPayout,
        @JsonProperty("AutoRenewal") Boolean autoRenewal,
        @JsonProperty("Status") String status
) {}
