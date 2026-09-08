package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsLoanApplyRequest(
        @JsonProperty("CustomerId") String customerId,
        @JsonProperty("LoanType") String loanType,
        @JsonProperty("RequestedAmount") BigDecimal requestedAmount,
        @JsonProperty("TenureMonths") Integer tenureMonths,
        @JsonProperty("MonthlyIncome") BigDecimal monthlyIncome,
        @JsonProperty("BranchCode") String branchCode
) {}
