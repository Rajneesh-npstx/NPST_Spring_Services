package com.bank.loan.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmortizationEngine {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");

    public record InstallmentPlan(
            int installmentNo,
            LocalDate dueDate,
            BigDecimal principalComponent,
            BigDecimal interestComponent,
            BigDecimal totalInstallment,
            BigDecimal endingBalance
    ) {}

    /**
     * Calculates Equated Monthly Installment (EMI) using reducing-balance formula:
     * EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     */
    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        if (principal == null || annualRatePercent == null || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthlyRate = annualRatePercent.divide(TWELVE_HUNDRED, MC);
        double r = monthlyRate.doubleValue();

        if (r == 0) {
            return principal.divide(new BigDecimal(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        double compoundFactor = Math.pow(1.0 + r, tenureMonths);
        double emiValue = principal.doubleValue() * r * (compoundFactor / (compoundFactor - 1.0));

        return new BigDecimal(String.valueOf(emiValue), MC).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Dynamically generates the month-by-month reducing-balance amortization schedule.
     */
    public List<InstallmentPlan> generateSchedule(BigDecimal principal,
                                                  BigDecimal annualRatePercent,
                                                  int tenureMonths,
                                                  LocalDate firstDueDate) {
        List<InstallmentPlan> schedule = new ArrayList<>();
        BigDecimal emi = calculateEMI(principal, annualRatePercent, tenureMonths);
        BigDecimal monthlyRate = annualRatePercent.divide(TWELVE_HUNDRED, MC);
        BigDecimal balance = principal.setScale(2, RoundingMode.HALF_UP);
        LocalDate dueDate = firstDueDate != null ? firstDueDate : LocalDate.now().plusMonths(1);

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestComponent = balance.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interestComponent).setScale(2, RoundingMode.HALF_UP);

            if (i == tenureMonths || principalComponent.compareTo(balance) > 0) {
                principalComponent = balance;
                emi = principalComponent.add(interestComponent);
                balance = BigDecimal.ZERO;
            } else {
                balance = balance.subtract(principalComponent).setScale(2, RoundingMode.HALF_UP);
            }

            schedule.add(new InstallmentPlan(
                    i,
                    dueDate,
                    principalComponent,
                    interestComponent,
                    emi,
                    balance
            ));

            dueDate = dueDate.plusMonths(1);
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        return schedule;
    }

    /**
     * Recalculates remaining loan parameters upon part-prepayment.
     */
    public record PrepaymentRecalculationResult(
            BigDecimal newPrincipal,
            BigDecimal newEmi,
            int newRemainingTenureMonths
    ) {}

    public PrepaymentRecalculationResult applyPrepayment(BigDecimal currentPrincipal,
                                                         BigDecimal prepaymentAmount,
                                                         BigDecimal annualRatePercent,
                                                         int currentRemainingTenureMonths,
                                                         BigDecimal currentEmi,
                                                         boolean reduceTenure) {
        BigDecimal newPrincipal = currentPrincipal.subtract(prepaymentAmount).max(BigDecimal.ZERO);
        if (newPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            return new PrepaymentRecalculationResult(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        if (reduceTenure) {
            // Keep EMI same, calculate lower tenure
            double r = annualRatePercent.divide(TWELVE_HUNDRED, MC).doubleValue();
            double p = newPrincipal.doubleValue();
            double emi = currentEmi.doubleValue();

            if (emi <= p * r) {
                // Cannot amortize if EMI does not even cover monthly interest
                return new PrepaymentRecalculationResult(newPrincipal, currentEmi, currentRemainingTenureMonths);
            }

            double months = -Math.log(1.0 - (r * p / emi)) / Math.log(1.0 + r);
            int newTenure = (int) Math.ceil(months);
            return new PrepaymentRecalculationResult(newPrincipal, currentEmi, Math.max(1, newTenure));
        } else {
            // Keep tenure same, calculate lower EMI
            BigDecimal newEmi = calculateEMI(newPrincipal, annualRatePercent, currentRemainingTenureMonths);
            return new PrepaymentRecalculationResult(newPrincipal, newEmi, currentRemainingTenureMonths);
        }
    }
}
