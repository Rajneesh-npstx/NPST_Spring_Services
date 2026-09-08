package com.bank.loan.engine;

import com.bank.loan.entity.LoanAccount;
import com.bank.loan.enums.InterestType;
import com.bank.loan.enums.LoanType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ForeclosureEngine {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal FORECLOSURE_RATE_FIXED = new BigDecimal("2.00"); // 2% for fixed/commercial loans

    public record ForeclosureSettlement(
            BigDecimal outstandingPrincipal,
            BigDecimal proRataInterest,
            BigDecimal foreclosureCharges,
            BigDecimal totalSettlementAmount,
            long daysSinceLastBilling,
            boolean chargeWaiverApplied
    ) {}

    public ForeclosureSettlement computeForeclosure(LoanAccount loan, LocalDate settlementDate) {
        LocalDate lastBillingDate = loan.getNextDueDate().minusMonths(1);
        long daysSinceLastBilling = Math.max(0, ChronoUnit.DAYS.between(lastBillingDate, settlementDate));

        // Pro-rata interest: Principal * (Rate / 100) * (days / 365)
        BigDecimal proRataInterest = loan.getOutstandingPrincipal()
                .multiply(loan.getInterestRate(), MC)
                .divide(HUNDRED, MC)
                .multiply(new BigDecimal(daysSinceLastBilling), MC)
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);

        // Regulatory check: RBI prohibits prepayment / foreclosure penalty on floating-rate individual loans
        boolean isFloatingHomeOrPersonal = loan.getInterestType() == InterestType.FLOATING
                && (loan.getLoanType() == LoanType.HOME_LOAN
                || loan.getLoanType() == LoanType.PERSONAL_LOAN
                || loan.getLoanType() == LoanType.AUTO_LOAN
                || loan.getLoanType() == LoanType.EDUCATION_LOAN);

        BigDecimal foreclosureCharges = BigDecimal.ZERO;
        boolean chargeWaiverApplied = false;

        if (isFloatingHomeOrPersonal) {
            chargeWaiverApplied = true;
        } else {
            // Fixed rate or commercial loan
            foreclosureCharges = loan.getOutstandingPrincipal()
                    .multiply(FORECLOSURE_RATE_FIXED, MC)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalSettlement = loan.getOutstandingPrincipal()
                .add(proRataInterest)
                .add(foreclosureCharges)
                .setScale(2, RoundingMode.HALF_UP);

        return new ForeclosureSettlement(
                loan.getOutstandingPrincipal(),
                proRataInterest,
                foreclosureCharges,
                totalSettlement,
                daysSinceLastBilling,
                chargeWaiverApplied
        );
    }
}
