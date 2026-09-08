package com.bank.loan.engine;

import com.bank.loan.entity.LoanAccount;
import com.bank.loan.entity.LoanRepayment;
import com.bank.loan.enums.InterestType;
import com.bank.loan.enums.LoanType;
import com.bank.loan.enums.RepaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanEngineTests {

    private final AmortizationEngine amortizationEngine = new AmortizationEngine();
    private final ForeclosureEngine foreclosureEngine = new ForeclosureEngine();
    private final TaxCertificateEngine taxCertificateEngine = new TaxCertificateEngine();

    @Test
    void testEmiCalculationAndSchedule() {
        BigDecimal principal = new BigDecimal("3000000.00"); // 30 Lakhs
        BigDecimal rate = new BigDecimal("8.50"); // 8.5%
        int tenureMonths = 180; // 15 years

        BigDecimal emi = amortizationEngine.calculateEMI(principal, rate, tenureMonths);
        assertNotNull(emi);
        // Standard EMI for 30L @ 8.5% for 180 months is approx 29,541.74
        assertTrue(emi.compareTo(new BigDecimal("29000.00")) > 0);
        assertTrue(emi.compareTo(new BigDecimal("30000.00")) < 0);

        var schedule = amortizationEngine.generateSchedule(principal, rate, tenureMonths, LocalDate.now());
        assertEquals(tenureMonths, schedule.size());
        assertEquals(1, schedule.get(0).installmentNo());
        assertEquals(0, schedule.get(schedule.size() - 1).endingBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testPartPrepayment() {
        BigDecimal principal = new BigDecimal("1000000.00");
        BigDecimal rate = new BigDecimal("9.00");
        int tenure = 60;
        BigDecimal emi = amortizationEngine.calculateEMI(principal, rate, tenure);

        // Prepay 2,00,000 with reduce tenure
        var resultTenure = amortizationEngine.applyPrepayment(
                principal, new BigDecimal("200000.00"), rate, tenure, emi, true);
        assertEquals(new BigDecimal("800000.00"), resultTenure.newPrincipal());
        assertTrue(resultTenure.newRemainingTenureMonths() < tenure);
        assertEquals(emi, resultTenure.newEmi());

        // Prepay 2,00,000 with reduce EMI
        var resultEmi = amortizationEngine.applyPrepayment(
                principal, new BigDecimal("200000.00"), rate, tenure, emi, false);
        assertEquals(new BigDecimal("800000.00"), resultEmi.newPrincipal());
        assertEquals(tenure, resultEmi.newRemainingTenureMonths());
        assertTrue(resultEmi.newEmi().compareTo(emi) < 0);
    }

    @Test
    void testForeclosureWithRbiWaiver() {
        LoanAccount floatingHomeLoan = LoanAccount.builder()
                .loanType(LoanType.HOME_LOAN)
                .interestType(InterestType.FLOATING)
                .outstandingPrincipal(new BigDecimal("2000000.00"))
                .interestRate(new BigDecimal("8.50"))
                .nextDueDate(LocalDate.now().plusDays(15))
                .build();

        var settlement = foreclosureEngine.computeForeclosure(floatingHomeLoan, LocalDate.now());
        assertTrue(settlement.chargeWaiverApplied());
        assertEquals(BigDecimal.ZERO, settlement.foreclosureCharges());
        assertTrue(settlement.totalSettlementAmount().compareTo(floatingHomeLoan.getOutstandingPrincipal()) >= 0);

        // Commercial / Fixed loan
        LoanAccount fixedCommercialLoan = LoanAccount.builder()
                .loanType(LoanType.COMMERCIAL_LOAN)
                .interestType(InterestType.FIXED)
                .outstandingPrincipal(new BigDecimal("1000000.00"))
                .interestRate(new BigDecimal("12.00"))
                .nextDueDate(LocalDate.now().plusDays(15))
                .build();

        var commercialSettlement = foreclosureEngine.computeForeclosure(fixedCommercialLoan, LocalDate.now());
        assertFalse(commercialSettlement.chargeWaiverApplied());
        // 2% foreclosure charge on 1,000,000 = 20,000
        assertEquals(new BigDecimal("20000.00"), commercialSettlement.foreclosureCharges());
    }

    @Test
    void testTaxCertificateBreakdown() {
        LoanAccount homeLoan = LoanAccount.builder()
                .loanAccountNumber("LN100001")
                .customerId("CIF100001")
                .loanType(LoanType.HOME_LOAN)
                .interestRate(new BigDecimal("8.50"))
                .outstandingPrincipal(new BigDecimal("2500000.00"))
                .emiAmount(new BigDecimal("30000.00"))
                .build();

        List<LoanRepayment> repayments = List.of(
                LoanRepayment.builder()
                        .paymentType(RepaymentType.EMI)
                        .totalAmount(new BigDecimal("30000.00"))
                        .principalComponent(new BigDecimal("12000.00"))
                        .interestComponent(new BigDecimal("18000.00"))
                        .paymentDate(LocalDate.of(2025, 5, 10))
                        .build(),
                LoanRepayment.builder()
                        .paymentType(RepaymentType.EMI)
                        .totalAmount(new BigDecimal("30000.00"))
                        .principalComponent(new BigDecimal("12500.00"))
                        .interestComponent(new BigDecimal("17500.00"))
                        .paymentDate(LocalDate.of(2025, 6, 10))
                        .build()
        );

        var tax = taxCertificateEngine.generateTaxBreakdown(homeLoan, repayments, "2026");
        assertEquals(new BigDecimal("24500.00"), tax.totalPrincipalPaid());
        assertEquals(new BigDecimal("24500.00"), tax.eligible80CAmount());
        assertEquals(new BigDecimal("35500.00"), tax.totalInterestPaid());
        assertEquals(new BigDecimal("35500.00"), tax.eligible24BAmount());
    }
}
