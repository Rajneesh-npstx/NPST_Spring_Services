package com.fund_transfer.backend.dto.Response;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponse(
        UUID id,
        String beneficiaryName,
        String beneficiaryAccountNumber,
        String beneficiaryIfscCode,
        String nickname,
        String transferMode,
        String status,
        Instant coolingPeriodEndsAt
) {
}