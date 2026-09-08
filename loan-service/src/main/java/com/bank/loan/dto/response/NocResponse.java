package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NocResponse(
        String nocCertificateNumber,
        String loanAccountNumber,
        String customerId,
        LocalDate closureDate,
        BigDecimal finalSettlementAmount,
        String lienReleaseStatus,
        String confirmationMessage
) {}
