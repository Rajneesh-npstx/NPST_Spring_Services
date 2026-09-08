package com.bank.loan.controller;

import com.bank.loan.dto.request.*;
import com.bank.loan.dto.response.*;
import com.bank.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyLoan(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @Valid @RequestBody ApplyLoanRequest request) {
        String key = idempotencyKeyHeader != null ? idempotencyKeyHeader : request.idempotencyKey();
        ApplyLoanRequest effectiveReq = new ApplyLoanRequest(
                request.customerId(),
                request.keycloakUserId(),
                request.loanType(),
                request.requestedAmount(),
                request.tenureMonths(),
                request.monthlyIncome(),
                request.branchCode(),
                request.interestType(),
                key
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyLoan(effectiveReq));
    }

    @GetMapping("/{loanAccountNumber}")
    public ResponseEntity<LoanAccountResponse> getLoanAccount(@PathVariable String loanAccountNumber) {
        return ResponseEntity.ok(loanService.getLoanAccount(loanAccountNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanAccountResponse>> getCustomerLoans(@PathVariable String customerId) {
        return ResponseEntity.ok(loanService.getCustomerLoans(customerId));
    }

    @GetMapping("/{loanAccountNumber}/schedule")
    public ResponseEntity<AmortizationScheduleResponse> getAmortizationSchedule(@PathVariable String loanAccountNumber) {
        return ResponseEntity.ok(loanService.getAmortizationSchedule(loanAccountNumber));
    }

    @PostMapping("/{loanAccountNumber}/autopay")
    public ResponseEntity<AutoPayResponse> registerAutoPay(
            @PathVariable String loanAccountNumber,
            @Valid @RequestBody RegisterAutoPayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.registerAutoPay(loanAccountNumber, request));
    }

    @PostMapping("/{loanAccountNumber}/disbursements")
    public ResponseEntity<DisbursementTrancheResponse> addDisbursementTranche(
            @PathVariable String loanAccountNumber,
            @Valid @RequestBody DisbursementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.addDisbursementTranche(loanAccountNumber, request));
    }

    @GetMapping("/{loanAccountNumber}/disbursements")
    public ResponseEntity<DisbursementScheduleResponse> getDisbursementSchedule(@PathVariable String loanAccountNumber) {
        return ResponseEntity.ok(loanService.getDisbursementSchedule(loanAccountNumber));
    }

    @PostMapping("/{loanAccountNumber}/prepayment")
    public ResponseEntity<LoanAccountResponse> partPrepayment(
            @PathVariable String loanAccountNumber,
            @Valid @RequestBody PartPrepaymentRequest request) {
        return ResponseEntity.ok(loanService.partPrepayment(loanAccountNumber, request));
    }

    @GetMapping("/{loanAccountNumber}/foreclosure/simulate")
    public ResponseEntity<ForeclosureStatementResponse> simulateForeclosure(
            @PathVariable String loanAccountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDate) {
        return ResponseEntity.ok(loanService.simulateForeclosure(loanAccountNumber, settlementDate));
    }

    @PostMapping("/{loanAccountNumber}/foreclosure/execute")
    public ResponseEntity<NocResponse> executeForeclosure(
            @PathVariable String loanAccountNumber,
            @Valid @RequestBody ForeclosureRequest request) {
        return ResponseEntity.ok(loanService.executeForeclosure(loanAccountNumber, request));
    }

    @GetMapping("/{loanAccountNumber}/tax-certificate")
    public ResponseEntity<LoanTaxCertificateResponse> getTaxCertificate(
            @PathVariable String loanAccountNumber,
            @RequestParam(defaultValue = "2026") String financialYear) {
        return ResponseEntity.ok(loanService.getTaxCertificate(loanAccountNumber, financialYear));
    }
}
