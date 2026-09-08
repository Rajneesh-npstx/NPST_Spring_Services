package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CbsTrialClosureRequest(
        @JsonProperty("TDAccountId") String tdAccountId
) {}
