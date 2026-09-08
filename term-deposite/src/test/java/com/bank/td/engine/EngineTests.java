package com.bank.td.engine;

import com.bank.td.entity.TermDeposit;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.FormStatus;
import com.bank.td.enums.FormType;
import com.bank.td.enums.InterestPayout;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EngineTests {

    private final InterestCalculationEngine interestEngine = new InterestCalculationEngine();
    private final PrematureClosureEngine prematureEngine = new PrematureClosureEngine();
    private final TaxEngine taxEngine = new TaxEngine();

    @Test
    void testCumulativeCompounding() {
        BigDecimal principal = new BigDecimal("100000.00");
        BigDecimal rate = new BigDecimal("7.00");
        int tenureMonths = 12; // 1 year = 4 quarters

        BigDecimal maturity = interestEngine.calculateMaturityAmount(
                principal, rate, tenureMonths, DepositType.FIXED_DEPOSIT, InterestPayout.ON_MATURITY);

        assertNotNull(maturity);
        assertTrue(maturity.compareTo(principal) > 0);
        // (1 + 0.07/4)^4 = 1.071859 -> approx 107185.90
        assertTrue(maturity.compareTo(new BigDecimal("107180.00")) > 0);
        assertTrue(maturity.compareTo(new BigDecimal("107200.00")) < 0);
    }

    @Test
    void testPrematureClosure() {
        TermDeposit deposit = TermDeposit.builder()
                .principalAmount(new BigDecimal("100000.00"))
                .interestRate(new BigDecimal("7.50"))
                .interestPayout(InterestPayout.ON_MATURITY)
                .depositDate(LocalDate.now().minusDays(200)) // held 200 days -> slab 5.50% - 1.00% penalty = 4.50%
                .build();

        var result = prematureEngine.calculatePrematureSettlement(deposit, LocalDate.now());
        assertEquals(200, result.daysHeld());
        assertEquals(new BigDecimal("4.50"), result.applicableRate());
        assertTrue(result.netPayable().compareTo(deposit.getPrincipalAmount()) > 0);
    }

    @Test
    void testTaxValidation() {
        // Senior citizen Form 15H valid
        var res15H = taxEngine.validateForm15(FormType.FORM_15H, 62, new BigDecimal("200000.00"));
        assertEquals(FormStatus.VERIFIED, res15H.status());

        // Under 60 trying Form 15H rejected
        var res15HInvalid = taxEngine.validateForm15(FormType.FORM_15H, 45, new BigDecimal("200000.00"));
        assertEquals(FormStatus.REJECTED, res15HInvalid.status());

        // Regular Section 194A TDS calculation
        var tdsRes = taxEngine.computeSection194ATds(new BigDecimal("60000.00"), true, 30, false);
        assertTrue(tdsRes.thresholdExceeded());
        assertEquals(new BigDecimal("6000.00"), tdsRes.tdsAmount()); // 10% of 60,000
    }
}
