package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.enums.TransferMode;

import java.time.Instant;
import java.util.UUID;

public record BeneficiaryResponse(
        UUID id,
        String beneficiaryName,
        String beneficiaryAccountNumber,
        String beneficiaryIfscCode,
        String nickname,
        TransferMode transferMode,
        BeneficiaryStatus status,
        Instant coolingPeriodEndsAt
) {
}