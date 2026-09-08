package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsLoanApplyResponse(
        @JsonProperty("ApplicationNumber") String applicationNumber,
        @JsonProperty("Status") String status,
        @JsonProperty("EstimatedEMI") BigDecimal estimatedEmi,
        @JsonProperty("Message") String message
) {}
