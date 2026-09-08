package com.fund_transfer.backend.dto.Request;

import com.fund_transfer.backend.enums.BeneficiaryType;
import com.fund_transfer.backend.enums.TransferMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigInteger;

public record CreateBeneficiaryRequest(

        @NotBlank
        String ownerCif,

        @NotBlank
        String beneficiaryName,

        @NotBlank
        String beneficiaryAccountNumber,

        @NotBlank
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "beneficiaryIfscCode must be a valid IFSC code"
        )
        String beneficiaryIfscCode,

        String nickname,

        @NotNull
        TransferMode transferMode,

        @NotNull
        BeneficiaryType type,

        BigInteger dailyLimitMinorUnits

) {
}