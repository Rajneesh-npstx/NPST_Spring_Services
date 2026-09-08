package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsLoanItem(
        @JsonProperty("LoanAccountNumber") String loanAccountNumber,
        @JsonProperty("LoanType") String loanType,
        @JsonProperty("SanctionedAmount") BigDecimal sanctionedAmount,
        @JsonProperty("OutstandingPrincipal") BigDecimal outstandingPrincipal,
        @JsonProperty("InterestRate") BigDecimal interestRate,
        @JsonProperty("TenureMonths") Integer tenureMonths,
        @JsonProperty("RemainingTenureMonths") Integer remainingTenureMonths,
        @JsonProperty("EMIAmount") BigDecimal emiAmount,
        @JsonProperty("NextDueDate") String nextDueDate,
        @JsonProperty("Status") String status
) {}
