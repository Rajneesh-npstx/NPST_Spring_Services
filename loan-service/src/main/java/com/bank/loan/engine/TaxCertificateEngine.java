package com.bank.loan.engine;

import com.bank.loan.entity.LoanAccount;
import com.bank.loan.entity.LoanRepayment;
import com.bank.loan.enums.LoanType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TaxCertificateEngine {

    private static final BigDecimal LIMIT_80C = new BigDecimal("150000.00");
    private static final BigDecimal LIMIT_24B = new BigDecimal("200000.00");

    public record LoanTaxCertificateBreakdown(
            String loanAccountNumber,
            String customerId,
            String financialYear,
            LoanType loanType,
            BigDecimal totalPrincipalPaid,
            BigDecimal eligible80CAmount,
            BigDecimal totalInterestPaid,
            BigDecimal eligible24BAmount
    ) {}

    public LoanTaxCertificateBreakdown generateTaxBreakdown(LoanAccount loan,
                                                           List<LoanRepayment> repayments,
                                                           String financialYear) {
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (LoanRepayment repayment : repayments) {
            if (repayment.getPrincipalComponent() != null) {
                totalPrincipal = totalPrincipal.add(repayment.getPrincipalComponent());
            }
            if (repayment.getInterestComponent() != null) {
                totalInterest = totalInterest.add(repayment.getInterestComponent());
            }
        }

        // If no past repayments recorded yet (e.g. simulated from schedule), project from sanctioned amount and EMI
        if (totalPrincipal.compareTo(BigDecimal.ZERO) == 0 && totalInterest.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal annualEmiTotal = loan.getEmiAmount().multiply(new BigDecimal("12"));
            BigDecimal annualInterest = loan.getOutstandingPrincipal()
                    .multiply(loan.getInterestRate())
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            totalInterest = annualInterest;
            totalPrincipal = annualEmiTotal.subtract(annualInterest).max(BigDecimal.ZERO);
        }

        BigDecimal eligible80C = totalPrincipal.min(LIMIT_80C);
        BigDecimal eligible24B = loan.getLoanType() == LoanType.HOME_LOAN
                ? totalInterest.min(LIMIT_24B)
                : BigDecimal.ZERO;

        return new LoanTaxCertificateBreakdown(
                loan.getLoanAccountNumber(),
                loan.getCustomerId(),
                financialYear,
                loan.getLoanType(),
                totalPrincipal,
                eligible80C,
                totalInterest,
                eligible24B
        );
    }
}
