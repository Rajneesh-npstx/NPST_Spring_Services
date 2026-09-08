package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsTrialClosureResponse(
        @JsonProperty("TDAccountId") String tdAccountId,
        @JsonProperty("ClosureValue") BigDecimal closureValue,
        @JsonProperty("PenaltyAmount") BigDecimal penaltyAmount,
        @JsonProperty("NetPayable") BigDecimal netPayable
) {}
