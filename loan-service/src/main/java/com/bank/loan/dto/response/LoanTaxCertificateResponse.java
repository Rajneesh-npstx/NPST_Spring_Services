package com.bank.loan.dto.response;

import com.bank.loan.enums.LoanType;
import java.math.BigDecimal;

public record LoanTaxCertificateResponse(
        String certificateNumber,
        String loanAccountNumber,
        String customerId,
        String financialYear,
        LoanType loanType,
        BigDecimal totalPrincipalRepaid,
        BigDecimal section80CEligibleAmount,
        BigDecimal totalInterestPaid,
        BigDecimal section24BEligibleAmount,
        String notes
) {}
