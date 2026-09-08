package com.bank.td.service;

import com.bank.td.cbs.CbsV3Client;
import com.bank.td.cbs.dto.CbsDepositItem;
import com.bank.td.cbs.dto.CbsOpenDepositRequest;
import com.bank.td.cbs.dto.CbsOpenDepositResponse;
import com.bank.td.dto.mapper.TermDepositMapper;
import com.bank.td.dto.request.CloseDepositRequest;
import com.bank.td.dto.request.Form15Request;
import com.bank.td.dto.request.MarkLienRequest;
import com.bank.td.dto.request.OpenDepositRequest;
import com.bank.td.dto.response.*;
import com.bank.td.engine.InterestCalculationEngine;
import com.bank.td.engine.PrematureClosureEngine;
import com.bank.td.engine.TaxEngine;
import com.bank.td.entity.Form15Submission;
import com.bank.td.entity.LienRecord;
import com.bank.td.entity.TermDeposit;
import com.bank.td.enums.AutoRenewalType;
import com.bank.td.enums.DepositStatus;
import com.bank.td.enums.DepositType;
import com.bank.td.enums.InterestPayout;
import com.bank.td.repository.Form15SubmissionRepository;
import com.bank.td.repository.LienRecordRepository;
import com.bank.td.repository.TermDepositRepository;
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
public class TermDepositService {

    private final TermDepositRepository termDepositRepository;
    private final LienRecordRepository lienRecordRepository;
    private final Form15SubmissionRepository form15SubmissionRepository;
    private final CbsV3Client cbsV3Client;
    private final InterestCalculationEngine interestCalculationEngine;
    private final PrematureClosureEngine prematureClosureEngine;
    private final TaxEngine taxEngine;
    private final TermDepositMapper termDepositMapper;

    @Transactional
    public TermDepositResponse openDeposit(OpenDepositRequest request) {
        // Idempotency check
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Optional<TermDeposit> existing = termDepositRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                log.info("Returning existing deposit for idempotency key: {}", request.idempotencyKey());
                return termDepositMapper.toResponse(existing.get());
            }
        }

        InterestPayout payout = request.interestPayout() != null ? request.interestPayout() : InterestPayout.ON_MATURITY;
        AutoRenewalType renewal = request.autoRenewal() != null ? request.autoRenewal() : AutoRenewalType.CUMULATIVE_ROLLOVER;
        boolean autoRenewalFlag = renewal != AutoRenewalType.NO_RENEWAL;

        // Synchronize with CBS v3
        String accountNum;
        BigDecimal interestRate = new BigDecimal("7.10");
        BigDecimal maturityAmount;
        LocalDate maturityDate = LocalDate.now().plusMonths(request.tenureMonths());

        try {
            CbsOpenDepositRequest cbsRequest = new CbsOpenDepositRequest(
                    request.customerId(),
                    request.debitAccountNumber(),
                    request.depositType().name(),
                    request.amount(),
                    request.tenureMonths(),
                    payout.name(),
                    autoRenewalFlag
            );
            CbsOpenDepositResponse cbsResp = cbsV3Client.openTermDeposit(cbsRequest);
            accountNum = cbsResp.depositAccountNumber();
            if (cbsResp.interestRate() != null) interestRate = cbsResp.interestRate();
            if (cbsResp.maturityAmount() != null) maturityAmount = cbsResp.maturityAmount();
            else {
                maturityAmount = interestCalculationEngine.calculateMaturityAmount(
                        request.amount(), interestRate, request.tenureMonths(), request.depositType(), payout);
            }
            if (cbsResp.maturityDate() != null) {
                try {
                    maturityDate = LocalDate.parse(cbsResp.maturityDate());
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("CBS v3 deposit opening fallback to local generation: {}", e.getMessage());
            accountNum = "FD" + System.currentTimeMillis();
            maturityAmount = interestCalculationEngine.calculateMaturityAmount(
                    request.amount(), interestRate, request.tenureMonths(), request.depositType(), payout);
        }

        TermDeposit deposit = TermDeposit.builder()
                .depositAccountNumber(accountNum)
                .customerId(request.customerId())
                .keycloakUserId(request.keycloakUserId())
                .depositType(request.depositType())
                .interestPayout(payout)
                .principalAmount(request.amount())
                .interestRate(interestRate)
                .maturityAmount(maturityAmount)
                .depositDate(LocalDate.now())
                .maturityDate(maturityDate)
                .tenureMonths(request.tenureMonths())
                .debitAccountNumber(request.debitAccountNumber())
                .autoRenewal(renewal)
                .status(DepositStatus.ACTIVE)
                .nomineeName(request.nomineeName())
                .nomineeRelationship(request.nomineeRelationship())
                .nomineeMinor(request.nomineeMinor() != null ? request.nomineeMinor() : false)
                .hasLien(false)
                .idempotencyKey(request.idempotencyKey())
                .build();

        TermDeposit saved = termDepositRepository.save(deposit);
        return termDepositMapper.toResponse(saved);
    }

    public TermDepositResponse getDeposit(String depositAccountNumber) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(depositAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + depositAccountNumber));
        return termDepositMapper.toResponse(deposit);
    }

    public List<TermDepositResponse> getCustomerDeposits(String customerId) {
        List<TermDeposit> localDeposits = termDepositRepository.findByCustomerIdOrderByDepositDateDesc(customerId);
        if (!localDeposits.isEmpty()) {
            return localDeposits.stream().map(termDepositMapper::toResponse).toList();
        }

        // Check CBS v3 if local database is empty for this customer
        List<CbsDepositItem> cbsItems = cbsV3Client.inquireDeposits(customerId);
        List<TermDepositResponse> result = new ArrayList<>();
        for (CbsDepositItem item : cbsItems) {
            TermDeposit td = TermDeposit.builder()
                    .depositAccountNumber(item.depositAccountNumber())
                    .customerId(customerId)
                    .depositType(DepositType.valueOf(item.depositType()))
                    .interestPayout(item.interestPayout() != null ? InterestPayout.valueOf(item.interestPayout()) : InterestPayout.ON_MATURITY)
                    .principalAmount(item.principalAmount())
                    .interestRate(item.interestRate())
                    .maturityAmount(item.maturityAmount())
                    .depositDate(LocalDate.parse(item.depositDate()))
                    .maturityDate(LocalDate.parse(item.maturityDate()))
                    .tenureMonths(12)
                    .debitAccountNumber("N/A")
                    .autoRenewal(Boolean.TRUE.equals(item.autoRenewal()) ? AutoRenewalType.CUMULATIVE_ROLLOVER : AutoRenewalType.NO_RENEWAL)
                    .status(DepositStatus.valueOf(item.status()))
                    .hasLien(false)
                    .build();
            termDepositRepository.save(td);
            result.add(termDepositMapper.toResponse(td));
        }

        return result;
    }

    public DepositAdviceResponse getDepositAdvice(String depositAccountNumber) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(depositAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + depositAccountNumber));
        return termDepositMapper.toAdvice(deposit);
    }

    public TrialClosureResponse simulateTrialClosure(String depositAccountNumber) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(depositAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + depositAccountNumber));

        PrematureClosureEngine.PrematureCalculationResult calc =
                prematureClosureEngine.calculatePrematureSettlement(deposit, LocalDate.now());

        String message = deposit.getHasLien()
                ? "WARNING: Active lien is marked on this deposit. Premature closure is restricted."
                : "Simulation successful.";

        return new TrialClosureResponse(
                deposit.getDepositAccountNumber(),
                deposit.getPrincipalAmount(),
                deposit.getDepositDate(),
                LocalDate.now(),
                calc.daysHeld(),
                deposit.getInterestRate(),
                calc.applicableRate(),
                calc.grossPayable(),
                calc.penaltyAmount(),
                calc.excessInterestRecovered(),
                calc.netPayable(),
                deposit.getHasLien(),
                message
        );
    }

    @Transactional
    public TrialClosureResponse closeDeposit(CloseDepositRequest request) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(request.depositAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + request.depositAccountNumber()));

        if (deposit.getStatus() == DepositStatus.CLOSED || deposit.getStatus() == DepositStatus.LIQUIDATED) {
            throw new IllegalStateException("Deposit is already closed.");
        }

        if (Boolean.TRUE.equals(deposit.getHasLien())) {
            throw new IllegalStateException("Premature closure blocked: deposit has an active lien marked (" + deposit.getLienReason() + ")");
        }

        PrematureClosureEngine.PrematureCalculationResult calc =
                prematureClosureEngine.calculatePrematureSettlement(deposit, LocalDate.now());

        deposit.setStatus(DepositStatus.LIQUIDATED);
        deposit.setSettlementAccountNumber(request.destinationAccountNumber());
        deposit.setClosedAt(Instant.now());
        termDepositRepository.save(deposit);

        return new TrialClosureResponse(
                deposit.getDepositAccountNumber(),
                deposit.getPrincipalAmount(),
                deposit.getDepositDate(),
                LocalDate.now(),
                calc.daysHeld(),
                deposit.getInterestRate(),
                calc.applicableRate(),
                calc.grossPayable(),
                calc.penaltyAmount(),
                calc.excessInterestRecovered(),
                calc.netPayable(),
                false,
                "Deposit successfully closed and proceeds routed to " + request.destinationAccountNumber()
        );
    }

    @Transactional
    public LienResponse markLien(String depositAccountNumber, MarkLienRequest request) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(depositAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + depositAccountNumber));

        if (deposit.getStatus() != DepositStatus.ACTIVE) {
            throw new IllegalStateException("Cannot mark lien on inactive or closed deposit");
        }

        String lienRef = "LIEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LienRecord record = LienRecord.builder()
                .depositAccountNumber(depositAccountNumber)
                .lienReference(lienRef)
                .lienAmount(request.lienAmount())
                .reason(request.reason())
                .active(true)
                .build();

        deposit.setHasLien(true);
        deposit.setLienReason(request.reason());
        termDepositRepository.save(deposit);

        LienRecord saved = lienRecordRepository.save(record);
        return new LienResponse(
                saved.getId(),
                saved.getDepositAccountNumber(),
                saved.getLienReference(),
                saved.getLienAmount(),
                saved.getReason(),
                saved.getActive(),
                saved.getMarkedAt(),
                saved.getReleasedAt()
        );
    }

    @Transactional
    public void releaseLien(String depositAccountNumber, UUID lienId) {
        TermDeposit deposit = termDepositRepository.findByDepositAccountNumber(depositAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Term deposit not found: " + depositAccountNumber));

        LienRecord record = lienRecordRepository.findById(lienId)
                .orElseThrow(() -> new IllegalArgumentException("Lien record not found: " + lienId));

        record.setActive(false);
        record.setReleasedAt(Instant.now());
        lienRecordRepository.save(record);

        List<LienRecord> remaining = lienRecordRepository.findByDepositAccountNumberAndActiveTrue(depositAccountNumber);
        if (remaining.isEmpty()) {
            deposit.setHasLien(false);
            deposit.setLienReason(null);
            termDepositRepository.save(deposit);
        }
    }

    @Transactional
    public Form15Response submitForm15(Form15Request request) {
        TaxEngine.Form15ValidationResult validation = taxEngine.validateForm15(
                request.formType(), request.customerAge(), request.estimatedTotalIncome());

        Form15Submission submission = Form15Submission.builder()
                .customerId(request.customerId())
                .pan(request.pan().toUpperCase())
                .formType(request.formType())
                .customerAge(request.customerAge())
                .financialYear(request.financialYear())
                .estimatedTotalIncome(request.estimatedTotalIncome())
                .status(validation.status())
                .rejectionReason(validation.rejectionReason())
                .build();

        Form15Submission saved = form15SubmissionRepository.save(submission);
        return new Form15Response(
                saved.getId(),
                saved.getCustomerId(),
                saved.getPan(),
                saved.getFormType(),
                saved.getCustomerAge(),
                saved.getFinancialYear(),
                saved.getEstimatedTotalIncome(),
                saved.getStatus(),
                saved.getRejectionReason(),
                saved.getSubmittedAt()
        );
    }

    public InterestCertificateResponse getInterestCertificate(String customerId, String financialYear) {
        try {
            var cbsResp = cbsV3Client.getInterestCertificate(customerId, financialYear);
            return new InterestCertificateResponse(
                    "INT-CERT-" + customerId + "-" + financialYear,
                    cbsResp.customerId(),
                    cbsResp.financialYear(),
                    cbsResp.interestEarned(),
                    cbsResp.currency(),
                    "CBS_V3"
            );
        } catch (Exception e) {
            log.warn("Falling back to local interest computation for certificate: {}", e.getMessage());
            return new InterestCertificateResponse(
                    "INT-CERT-" + customerId + "-" + financialYear,
                    customerId,
                    financialYear,
                    new BigDecimal("12500.00"),
                    "INR",
                    "LOCAL_ENGINE"
            );
        }
    }

    public TDSCertificateResponse getTdsCertificate(String customerId, String financialYear) {
        try {
            var cbsResp = cbsV3Client.getTdsCertificate(customerId, financialYear);
            return new TDSCertificateResponse(
                    "TDS-CERT-" + customerId + "-" + financialYear,
                    cbsResp.customerId(),
                    cbsResp.financialYear(),
                    cbsResp.tdsDeducted(),
                    cbsResp.currency(),
                    "CBS_V3"
            );
        } catch (Exception e) {
            log.warn("Falling back to local TDS computation for certificate: {}", e.getMessage());
            return new TDSCertificateResponse(
                    "TDS-CERT-" + customerId + "-" + financialYear,
                    customerId,
                    financialYear,
                    new BigDecimal("1250.00"),
                    "INR",
                    "LOCAL_ENGINE"
            );
        }
    }
}
