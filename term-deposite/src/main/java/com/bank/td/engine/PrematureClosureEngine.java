package com.bank.td.engine;

import com.bank.td.entity.TermDeposit;
import com.bank.td.enums.InterestPayout;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PrematureClosureEngine {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal PENALTY_RATE = new BigDecimal("1.00"); // 1% premature penalty

    public record PrematureCalculationResult(
            BigDecimal grossPayable,
            BigDecimal penaltyAmount,
            BigDecimal excessInterestRecovered,
            BigDecimal netPayable,
            BigDecimal applicableRate,
            long daysHeld
    ) {}

    public PrematureCalculationResult calculatePrematureSettlement(TermDeposit deposit, LocalDate settlementDate) {
        long daysHeld = ChronoUnit.DAYS.between(deposit.getDepositDate(), settlementDate);
        if (daysHeld < 7) {
            // Under RBI rules, deposits closed within 7 days earn no interest
            return new PrematureCalculationResult(
                    deposit.getPrincipalAmount(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    deposit.getPrincipalAmount(),
                    BigDecimal.ZERO,
                    daysHeld
            );
        }

        BigDecimal slabRate = lookupSlabRate(daysHeld);
        // Effective rate = slab rate - 1.00% penalty
        BigDecimal effectiveRate = slabRate.subtract(PENALTY_RATE).max(BigDecimal.ZERO);

        // Simple interest on actual held period: Principal * (effectiveRate / 100) * (daysHeld / 365)
        BigDecimal interestEarned = deposit.getPrincipalAmount()
                .multiply(effectiveRate, MC)
                .divide(HUNDRED, MC)
                .multiply(new BigDecimal(daysHeld), MC)
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);

        BigDecimal grossPayable = deposit.getPrincipalAmount().add(interestEarned);

        // Penalty difference
        BigDecimal regularInterest = deposit.getPrincipalAmount()
                .multiply(slabRate, MC)
                .divide(HUNDRED, MC)
                .multiply(new BigDecimal(daysHeld), MC)
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);
        BigDecimal penaltyAmount = regularInterest.subtract(interestEarned).max(BigDecimal.ZERO);

        BigDecimal excessInterestRecovered = BigDecimal.ZERO;
        if (deposit.getInterestPayout() != InterestPayout.ON_MATURITY) {
            // For non-cumulative payout, calculate estimated interest already paid out and recover difference
            long monthsHeld = ChronoUnit.MONTHS.between(deposit.getDepositDate(), settlementDate);
            BigDecimal contractedAnnual = deposit.getPrincipalAmount()
                    .multiply(deposit.getInterestRate(), MC)
                    .divide(HUNDRED, MC);
            BigDecimal paidOut = contractedAnnual.multiply(new BigDecimal(monthsHeld), MC)
                    .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

            if (paidOut.compareTo(interestEarned) > 0) {
                excessInterestRecovered = paidOut.subtract(interestEarned);
            }
        }

        BigDecimal netPayable = deposit.getPrincipalAmount()
                .add(interestEarned)
                .subtract(excessInterestRecovered)
                .setScale(2, RoundingMode.HALF_UP);

        return new PrematureCalculationResult(
                grossPayable,
                penaltyAmount,
                excessInterestRecovered,
                netPayable,
                effectiveRate,
                daysHeld
        );
    }

    private BigDecimal lookupSlabRate(long daysHeld) {
        if (daysHeld < 180) {
            return new BigDecimal("4.50");
        } else if (daysHeld < 365) {
            return new BigDecimal("5.50");
        } else if (daysHeld < 730) {
            return new BigDecimal("6.50");
        } else {
            return new BigDecimal("7.00");
        }
    }
}
