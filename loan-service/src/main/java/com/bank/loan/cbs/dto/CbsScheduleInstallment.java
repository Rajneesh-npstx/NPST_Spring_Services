package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsScheduleInstallment(
        @JsonProperty("InstallmentNo") Integer installmentNo,
        @JsonProperty("DueDate") String dueDate,
        @JsonProperty("PrincipalComponent") BigDecimal principalComponent,
        @JsonProperty("InterestComponent") BigDecimal interestComponent,
        @JsonProperty("TotalInstallment") BigDecimal totalInstallment,
        @JsonProperty("EndingBalance") BigDecimal endingBalance
) {}
