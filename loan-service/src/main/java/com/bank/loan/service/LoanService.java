package com.bank.loan.service;

import com.bank.loan.cbs.CbsV3LoanClient;
import com.bank.loan.cbs.dto.CbsLoanApplyRequest;
import com.bank.loan.cbs.dto.CbsLoanApplyResponse;
import com.bank.loan.cbs.dto.CbsLoanItem;
import com.bank.loan.cbs.dto.CbsScheduleInstallment;
import com.bank.loan.dto.mapper.LoanMapper;
import com.bank.loan.dto.request.*;
import com.bank.loan.dto.response.*;
import com.bank.loan.engine.AmortizationEngine;
import com.bank.loan.engine.ForeclosureEngine;
import com.bank.loan.engine.TaxCertificateEngine;
import com.bank.loan.entity.*;
import com.bank.loan.enums.*;
import com.bank.loan.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final AutoPayMandateRepository autoPayMandateRepository;
    private final DisbursementTrancheRepository disbursementTrancheRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final CbsV3LoanClient cbsV3LoanClient;
    private final AmortizationEngine amortizationEngine;
    private final ForeclosureEngine foreclosureEngine;
    private final TaxCertificateEngine taxCertificateEngine;
    private final LoanMapper loanMapper;

    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("8.50");

    @Transactional
    public LoanApplicationResponse applyLoan(ApplyLoanRequest request) {
        // Idempotency check
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Optional<LoanAccount> existing = loanAccountRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                LoanAccount loan = existing.get();
                return new LoanApplicationResponse(
                        loan.getId(),
                        "APP-" + loan.getLoanAccountNumber(),
                        loan.getCustomerId(),
                        loan.getLoanType(),
                        loan.getSanctionedAmount(),
                        loan.getTenureMonths(),
                        request.monthlyIncome(),
                        loan.getBranchCode(),
                        loan.getStatus(),
                        loan.getEmiAmount(),
                        "Returning existing application for idempotency key",
                        loan.getCreatedAt()
                );
            }
        }

        BigDecimal emi = amortizationEngine.calculateEMI(
                request.requestedAmount(), DEFAULT_INTEREST_RATE, request.tenureMonths());

        String appNumber = "LNAPP" + System.currentTimeMillis();
        String cbsMessage = "Application registered successfully";
        LoanStatus appStatus = LoanStatus.IN_REVIEW;

        try {
            CbsLoanApplyRequest cbsReq = new CbsLoanApplyRequest(
                    request.customerId(),
                    request.loanType().name(),
                    request.requestedAmount(),
                    request.tenureMonths(),
                    request.monthlyIncome(),
                    request.branchCode() != null ? request.branchCode() : "001"
            );
            CbsLoanApplyResponse cbsResp = cbsV3LoanClient.applyLoan(cbsReq);
            if (cbsResp != null) {
                if (cbsResp.applicationNumber() != null) appNumber = cbsResp.applicationNumber();
                if (cbsResp.status() != null) appStatus = LoanStatus.valueOf(cbsResp.status());
                if (cbsResp.estimatedEmi() != null) emi = cbsResp.estimatedEmi();
                if (cbsResp.message() != null) cbsMessage = cbsResp.message();
            }
        } catch (Exception e) {
            log.warn("CBS v3 loan application call failed, proceeding with local registration: {}", e.getMessage());
        }

        LoanApplication application = LoanApplication.builder()
                .applicationNumber(appNumber)
                .customerId(request.customerId())
                .loanType(request.loanType())
                .requestedAmount(request.requestedAmount())
                .tenureMonths(request.tenureMonths())
                .monthlyIncome(request.monthlyIncome())
                .branchCode(request.branchCode() != null ? request.branchCode() : "001")
                .status(appStatus)
                .estimatedEmi(emi)
                .build();
        LoanApplication savedApp = loanApplicationRepository.save(application);

        // Provision active loan account so borrower can immediately track and simulate
        String loanAccountNum = "LN" + (System.currentTimeMillis() % 10000000000L);
        LoanAccount account = LoanAccount.builder()
                .loanAccountNumber(loanAccountNum)
                .customerId(request.customerId())
                .keycloakUserId(request.keycloakUserId())
                .loanType(request.loanType())
                .sanctionedAmount(request.requestedAmount())
                .disbursedAmount(request.requestedAmount())
                .outstandingPrincipal(request.requestedAmount())
                .interestRate(DEFAULT_INTEREST_RATE)
                .tenureMonths(request.tenureMonths())
                .remainingTenureMonths(request.tenureMonths())
                .emiAmount(emi)
                .nextDueDate(LocalDate.now().plusMonths(1))
                .status(LoanStatus.ACTIVE)
                .interestType(request.interestType() != null ? request.interestType() : InterestType.FLOATING)
                .branchCode(request.branchCode() != null ? request.branchCode() : "001")
                .idempotencyKey(request.idempotencyKey())
                .build();
        loanAccountRepository.save(account);

        return loanMapper.toApplicationResponse(savedApp, cbsMessage + ". Provisioned Loan Account: " + loanAccountNum);
    }

    public LoanAccountResponse getLoanAccount(String loanAccountNumber) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));
        return loanMapper.toResponse(loan);
    }

    public List<LoanAccountResponse> getCustomerLoans(String customerId) {
        List<LoanAccount> localLoans = loanAccountRepository.findByCustomerId(customerId);
        if (!localLoans.isEmpty()) {
            return localLoans.stream().map(loanMapper::toResponse).toList();
        }

        // Check CBS v3
        List<CbsLoanItem> cbsLoans = cbsV3LoanClient.inquireLoans(customerId);
        List<LoanAccountResponse> result = new ArrayList<>();
        for (CbsLoanItem item : cbsLoans) {
            LoanAccount acc = LoanAccount.builder()
                    .loanAccountNumber(item.loanAccountNumber())
                    .customerId(customerId)
                    .loanType(LoanType.valueOf(item.loanType()))
                    .sanctionedAmount(item.sanctionedAmount())
                    .disbursedAmount(item.sanctionedAmount())
                    .outstandingPrincipal(item.outstandingPrincipal())
                    .interestRate(item.interestRate())
                    .tenureMonths(item.tenureMonths())
                    .remainingTenureMonths(item.remainingTenureMonths())
                    .emiAmount(item.emiAmount())
                    .nextDueDate(LocalDate.parse(item.nextDueDate()))
                    .status(LoanStatus.valueOf(item.status()))
                    .interestType(InterestType.FLOATING)
                    .branchCode("001")
                    .build();
            loanAccountRepository.save(acc);
            result.add(loanMapper.toResponse(acc));
        }

        return result;
    }

    public AmortizationScheduleResponse getAmortizationSchedule(String loanAccountNumber) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        // First attempt fetching CBS schedule
        List<CbsScheduleInstallment> cbsSchedule = cbsV3LoanClient.fetchAmortizationSchedule(loanAccountNumber);
        if (!cbsSchedule.isEmpty()) {
            List<ScheduleItemResponse> items = cbsSchedule.stream()
                    .map(s -> new ScheduleItemResponse(
                            s.installmentNo(),
                            LocalDate.parse(s.dueDate()),
                            s.principalComponent(),
                            s.interestComponent(),
                            s.totalInstallment(),
                            s.endingBalance()
                    )).toList();

            return new AmortizationScheduleResponse(
                    loan.getLoanAccountNumber(),
                    loan.getSanctionedAmount(),
                    loan.getOutstandingPrincipal(),
                    loan.getInterestRate(),
                    loan.getTenureMonths(),
                    loan.getRemainingTenureMonths(),
                    loan.getEmiAmount(),
                    "CBS_V3",
                    items
            );
        }

        // Dynamic calculation using reducing-balance amortization engine
        List<AmortizationEngine.InstallmentPlan> generated = amortizationEngine.generateSchedule(
                loan.getOutstandingPrincipal(),
                loan.getInterestRate(),
                loan.getRemainingTenureMonths(),
                loan.getNextDueDate()
        );

        List<ScheduleItemResponse> items = generated.stream()
                .map(g -> new ScheduleItemResponse(
                        g.installmentNo(),
                        g.dueDate(),
                        g.principalComponent(),
                        g.interestComponent(),
                        g.totalInstallment(),
                        g.endingBalance()
                )).toList();

        return new AmortizationScheduleResponse(
                loan.getLoanAccountNumber(),
                loan.getSanctionedAmount(),
                loan.getOutstandingPrincipal(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getRemainingTenureMonths(),
                loan.getEmiAmount(),
                "LOCAL_REDUCING_BALANCE_ENGINE",
                items
        );
    }

    @Transactional
    public AutoPayResponse registerAutoPay(String loanAccountNumber, RegisterAutoPayRequest request) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        // Deactivate existing active mandate if any
        autoPayMandateRepository.findByLoanAccountNumberAndStatus(loanAccountNumber, MandateStatus.ACTIVE)
                .ifPresent(m -> {
                    m.setStatus(MandateStatus.CANCELLED);
                    autoPayMandateRepository.save(m);
                });

        String mandateRef = "MANDATE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal maxDebit = request.maxDebitAmount() != null
                ? request.maxDebitAmount()
                : loan.getEmiAmount().multiply(new BigDecimal("1.25")); // 125% buffer for interest fluctuations

        AutoPayMandate mandate = AutoPayMandate.builder()
                .mandateReference(mandateRef)
                .loanAccountNumber(loanAccountNumber)
                .customerId(loan.getCustomerId())
                .debitAccountNumber(request.debitAccountNumber())
                .debitDayOfMonth(request.debitDayOfMonth())
                .maxDebitAmount(maxDebit)
                .status(MandateStatus.ACTIVE)
                .build();

        loan.setLinkedDebitAccount(request.debitAccountNumber());
        loanAccountRepository.save(loan);

        AutoPayMandate saved = autoPayMandateRepository.save(mandate);
        return new AutoPayResponse(
                saved.getId(),
                saved.getMandateReference(),
                saved.getLoanAccountNumber(),
                saved.getCustomerId(),
                saved.getDebitAccountNumber(),
                saved.getDebitDayOfMonth(),
                saved.getMaxDebitAmount(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public DisbursementTrancheResponse addDisbursementTranche(String loanAccountNumber, DisbursementRequest request) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        BigDecimal newTotalDisbursed = loan.getDisbursedAmount().add(request.amount());
        if (newTotalDisbursed.compareTo(loan.getSanctionedAmount()) > 0) {
            throw new IllegalArgumentException("Total disbursements cannot exceed sanctioned amount: " + loan.getSanctionedAmount());
        }

        // Pre-EMI interest: Amount * (Rate / 1200)
        BigDecimal preEmi = request.amount()
                .multiply(loan.getInterestRate())
                .divide(new BigDecimal("1200"), 2, java.math.RoundingMode.HALF_UP);

        LocalDate date = request.disbursementDate() != null ? request.disbursementDate() : LocalDate.now();

        DisbursementTranche tranche = DisbursementTranche.builder()
                .loanAccountNumber(loanAccountNumber)
                .trancheNumber(request.trancheNumber())
                .amount(request.amount())
                .disbursementDate(date)
                .status("DISBURSED")
                .preEmiInterest(preEmi)
                .build();

        loan.setDisbursedAmount(newTotalDisbursed);
        loan.setOutstandingPrincipal(loan.getOutstandingPrincipal().add(request.amount()));
        loanAccountRepository.save(loan);

        DisbursementTranche saved = disbursementTrancheRepository.save(tranche);
        return new DisbursementTrancheResponse(
                saved.getTrancheNumber(),
                saved.getAmount(),
                saved.getDisbursementDate(),
                saved.getStatus(),
                saved.getPreEmiInterest()
        );
    }

    public DisbursementScheduleResponse getDisbursementSchedule(String loanAccountNumber) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        List<DisbursementTranche> tranches = disbursementTrancheRepository
                .findByLoanAccountNumberOrderByTrancheNumberAsc(loanAccountNumber);

        List<DisbursementTrancheResponse> trancheResponses = tranches.stream()
                .map(t -> new DisbursementTrancheResponse(
                        t.getTrancheNumber(),
                        t.getAmount(),
                        t.getDisbursementDate(),
                        t.getStatus(),
                        t.getPreEmiInterest()))
                .toList();

        BigDecimal pendingAmount = loan.getSanctionedAmount().subtract(loan.getDisbursedAmount()).max(BigDecimal.ZERO);

        return new DisbursementScheduleResponse(
                loan.getLoanAccountNumber(),
                loan.getSanctionedAmount(),
                loan.getDisbursedAmount(),
                pendingAmount,
                trancheResponses
        );
    }

    @Transactional
    public LoanAccountResponse partPrepayment(String loanAccountNumber, PartPrepaymentRequest request) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Cannot prepay on inactive/closed loan");
        }

        if (request.prepaymentAmount().compareTo(loan.getOutstandingPrincipal()) >= 0) {
            throw new IllegalArgumentException("Prepayment amount meets or exceeds outstanding balance. Please use Foreclosure instead.");
        }

        boolean reduceTenure = request.action() == PrepaymentAction.REDUCE_TENURE;
        var recalc = amortizationEngine.applyPrepayment(
                loan.getOutstandingPrincipal(),
                request.prepaymentAmount(),
                loan.getInterestRate(),
                loan.getRemainingTenureMonths(),
                loan.getEmiAmount(),
                reduceTenure
        );

        loan.setOutstandingPrincipal(recalc.newPrincipal());
        loan.setEmiAmount(recalc.newEmi());
        loan.setRemainingTenureMonths(recalc.newRemainingTenureMonths());

        // Record repayment
        LoanRepayment repayment = LoanRepayment.builder()
                .paymentReference("PREPAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .loanAccountNumber(loanAccountNumber)
                .paymentType(RepaymentType.PART_PREPAYMENT)
                .totalAmount(request.prepaymentAmount())
                .principalComponent(request.prepaymentAmount())
                .interestComponent(BigDecimal.ZERO)
                .penalCharges(BigDecimal.ZERO)
                .paymentDate(LocalDate.now())
                .build();
        loanRepaymentRepository.save(repayment);

        LoanAccount updated = loanAccountRepository.save(loan);
        return loanMapper.toResponse(updated);
    }

    public ForeclosureStatementResponse simulateForeclosure(String loanAccountNumber, LocalDate settlementDate) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        LocalDate date = settlementDate != null ? settlementDate : LocalDate.now();
        var settlement = foreclosureEngine.computeForeclosure(loan, date);

        String waiverNotes = settlement.chargeWaiverApplied()
                ? "RBI Mandate Applied: 0% foreclosure penalty on floating-rate individual loan."
                : "2% foreclosure charge applied for fixed-rate / commercial facility.";

        return new ForeclosureStatementResponse(
                "FC-STMT-" + loanAccountNumber + "-" + date.format(DateTimeFormatter.BASIC_ISO_DATE),
                loan.getLoanAccountNumber(),
                loan.getCustomerId(),
                date,
                settlement.outstandingPrincipal(),
                settlement.proRataInterest(),
                settlement.foreclosureCharges(),
                settlement.totalSettlementAmount(),
                settlement.chargeWaiverApplied(),
                waiverNotes
        );
    }

    @Transactional
    public NocResponse executeForeclosure(String loanAccountNumber, ForeclosureRequest request) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        if (loan.getStatus() == LoanStatus.CLOSED || loan.getStatus() == LoanStatus.FORECLOSED) {
            throw new IllegalStateException("Loan is already closed or foreclosed");
        }

        LocalDate date = request.settlementDate() != null ? request.settlementDate() : LocalDate.now();
        var settlement = foreclosureEngine.computeForeclosure(loan, date);

        // Record full payment
        LoanRepayment repayment = LoanRepayment.builder()
                .paymentReference("FC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .loanAccountNumber(loanAccountNumber)
                .paymentType(RepaymentType.FORECLOSURE)
                .totalAmount(settlement.totalSettlementAmount())
                .principalComponent(settlement.outstandingPrincipal())
                .interestComponent(settlement.proRataInterest())
                .penalCharges(settlement.foreclosureCharges())
                .paymentDate(date)
                .build();
        loanRepaymentRepository.save(repayment);

        // Update loan status
        loan.setStatus(LoanStatus.FORECLOSED);
        loan.setOutstandingPrincipal(BigDecimal.ZERO);
        loan.setRemainingTenureMonths(0);
        loan.setClosedAt(Instant.now());
        loanAccountRepository.save(loan);

        // Cancel active auto-pay mandate
        autoPayMandateRepository.findByLoanAccountNumberAndStatus(loanAccountNumber, MandateStatus.ACTIVE)
                .ifPresent(m -> {
                    m.setStatus(MandateStatus.CANCELLED);
                    autoPayMandateRepository.save(m);
                });

        return new NocResponse(
                "NOC-BB-" + loanAccountNumber + "-" + System.currentTimeMillis(),
                loan.getLoanAccountNumber(),
                loan.getCustomerId(),
                date,
                settlement.totalSettlementAmount(),
                "RELEASED",
                "Bharat Bank certifies that loan facility " + loanAccountNumber + " has been fully settled and closed. All asset liens are formally released."
        );
    }

    public LoanTaxCertificateResponse getTaxCertificate(String loanAccountNumber, String financialYear) {
        LoanAccount loan = loanAccountRepository.findByLoanAccountNumber(loanAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found: " + loanAccountNumber));

        int fyYear = 2026;
        try {
            fyYear = Integer.parseInt(financialYear);
        } catch (Exception ignored) {}

        LocalDate start = LocalDate.of(fyYear - 1, 4, 1);
        LocalDate end = LocalDate.of(fyYear, 3, 31);

        List<LoanRepayment> repayments = loanRepaymentRepository
                .findRepaymentsInFinancialYear(loanAccountNumber, start, end);

        var breakdown = taxCertificateEngine.generateTaxBreakdown(loan, repayments, financialYear);

        String notes = loan.getLoanType() == LoanType.HOME_LOAN
                ? "Principal repayment is eligible under Section 80C (up to ₹1,50,000). Interest paid is eligible under Section 24(b) (up to ₹2,00,000) for self-occupied residential property."
                : "Principal repayment eligible under Section 80C if applicable. Consult your tax advisor.";

        return new LoanTaxCertificateResponse(
                "TAX-CERT-" + loanAccountNumber + "-" + financialYear,
                breakdown.loanAccountNumber(),
                breakdown.customerId(),
                breakdown.financialYear(),
                breakdown.loanType(),
                breakdown.totalPrincipalPaid(),
                breakdown.eligible80CAmount(),
                breakdown.totalInterestPaid(),
                breakdown.eligible24BAmount(),
                notes
        );
    }
}
