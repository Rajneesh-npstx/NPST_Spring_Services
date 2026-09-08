package com.bank.td.engine;

import com.bank.td.enums.FormStatus;
import com.bank.td.enums.FormType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class TaxEngine {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal TDS_THRESHOLD_REGULAR = new BigDecimal("40000.00");
    private static final BigDecimal TDS_THRESHOLD_SENIOR = new BigDecimal("50000.00");
    private static final BigDecimal BASIC_EXEMPTION_LIMIT = new BigDecimal("250000.00");
    private static final BigDecimal RATE_WITH_PAN = new BigDecimal("0.10"); // 10%
    private static final BigDecimal RATE_WITHOUT_PAN = new BigDecimal("0.20"); // 20%

    public record TdsComputationResult(
            BigDecimal totalInterest,
            BigDecimal thresholdLimit,
            boolean isExemptByForm15,
            boolean thresholdExceeded,
            BigDecimal tdsRate,
            BigDecimal tdsAmount
    ) {}

    public TdsComputationResult computeSection194ATds(BigDecimal annualInterest,
                                                      boolean hasValidPan,
                                                      int age,
                                                      boolean hasApprovedForm15) {
        if (hasApprovedForm15) {
            return new TdsComputationResult(
                    annualInterest,
                    BigDecimal.ZERO,
                    true,
                    false,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal threshold = age >= 60 ? TDS_THRESHOLD_SENIOR : TDS_THRESHOLD_REGULAR;
        boolean thresholdExceeded = annualInterest.compareTo(threshold) > 0;

        if (!thresholdExceeded) {
            return new TdsComputationResult(
                    annualInterest,
                    threshold,
                    false,
                    false,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal tdsRate = hasValidPan ? RATE_WITH_PAN : RATE_WITHOUT_PAN;
        BigDecimal tdsAmount = annualInterest.multiply(tdsRate, MC).setScale(2, RoundingMode.HALF_UP);

        return new TdsComputationResult(
                annualInterest,
                threshold,
                false,
                true,
                tdsRate.multiply(new BigDecimal("100")),
                tdsAmount
        );
    }

    public record Form15ValidationResult(
            FormStatus status,
            String rejectionReason
    ) {}

    public Form15ValidationResult validateForm15(FormType formType, int age, BigDecimal estimatedTotalIncome) {
        if (formType == FormType.FORM_15H) {
            if (age < 60) {
                return new Form15ValidationResult(FormStatus.REJECTED, "Form 15H is strictly for Senior Citizens (Age 60 and above)");
            }
            return new Form15ValidationResult(FormStatus.VERIFIED, null);
        } else if (formType == FormType.FORM_15G) {
            if (age >= 60) {
                return new Form15ValidationResult(FormStatus.REJECTED, "Senior Citizens (Age 60+) should submit Form 15H instead of Form 15G");
            }
            if (estimatedTotalIncome != null && estimatedTotalIncome.compareTo(BASIC_EXEMPTION_LIMIT) > 0) {
                return new Form15ValidationResult(FormStatus.REJECTED, "Estimated total income exceeds the basic tax exemption limit (₹2,50,000)");
            }
            return new Form15ValidationResult(FormStatus.VERIFIED, null);
        }

        return new Form15ValidationResult(FormStatus.REJECTED, "Unknown form type");
    }
}
