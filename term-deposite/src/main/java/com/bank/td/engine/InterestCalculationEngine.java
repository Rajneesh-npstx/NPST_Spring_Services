package com.bank.td.engine;

import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class InterestCalculationEngine {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal FOUR = new BigDecimal("4");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    /**
     * Calculates maturity amount for Fixed Deposits and Recurring Deposits.
     */
    public BigDecimal calculateMaturityAmount(BigDecimal principal,
                                            BigDecimal annualRatePercent,
                                            int tenureMonths,
                                            DepositType depositType,
                                            InterestPayout interestPayout) {
        if (principal == null || annualRatePercent == null || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        if (depositType == DepositType.RECURRING_DEPOSIT) {
            return calculateRdMaturity(principal, annualRatePercent, tenureMonths);
        }

        // Fixed Deposit
        if (interestPayout == InterestPayout.ON_MATURITY) {
            // Quarterly compounding: P * (1 + r/400)^(4 * t) where t = tenureMonths / 12
            BigDecimal rPerQuarter = annualRatePercent.divide(new BigDecimal("400"), MC);
            BigDecimal onePlusR = BigDecimal.ONE.add(rPerQuarter, MC);

            double numberOfQuarters = tenureMonths / 3.0;
            double factor = Math.pow(onePlusR.doubleValue(), numberOfQuarters);

            return principal.multiply(new BigDecimal(String.valueOf(factor), MC), MC)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            // Non-cumulative payout: principal returned at maturity, interest paid periodically
            return principal.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculates periodic interest payout for non-cumulative deposits.
     */
    public BigDecimal calculatePeriodicPayout(BigDecimal principal,
                                              BigDecimal annualRatePercent,
                                              InterestPayout interestPayout) {
        if (principal == null || annualRatePercent == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal annualInterest = principal.multiply(annualRatePercent, MC)
                .divide(HUNDRED, MC);

        if (interestPayout == InterestPayout.MONTHLY) {
            // Monthly payout = Annual / 12 (discounted yield equivalent)
            return annualInterest.divide(TWELVE, 2, RoundingMode.HALF_UP);
        } else if (interestPayout == InterestPayout.QUARTERLY) {
            // Quarterly payout = Annual / 4
            return annualInterest.divide(FOUR, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Recurring Deposit (RD) quarterly compounding on monthly installment principal.
     */
    public BigDecimal calculateRdMaturity(BigDecimal monthlyInstallment,
                                          BigDecimal annualRatePercent,
                                          int tenureMonths) {
        BigDecimal totalMaturity = BigDecimal.ZERO;
        BigDecimal rPerQuarter = annualRatePercent.divide(new BigDecimal("400"), MC);
        BigDecimal onePlusR = BigDecimal.ONE.add(rPerQuarter, MC);

        for (int month = 1; month <= tenureMonths; month++) {
            // Installment deposited at beginning of month: held for (tenureMonths - month + 1) months
            int monthsHeld = tenureMonths - month + 1;
            double quarters = monthsHeld / 3.0;
            double factor = Math.pow(onePlusR.doubleValue(), quarters);
            BigDecimal maturityOfTranche = monthlyInstallment.multiply(new BigDecimal(String.valueOf(factor), MC), MC);
            totalMaturity = totalMaturity.add(maturityOfTranche, MC);
        }

        return totalMaturity.setScale(2, RoundingMode.HALF_UP);
    }
}
