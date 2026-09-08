package com.bank.td.controller;

import com.bank.td.dto.request.CloseDepositRequest;
import com.bank.td.dto.request.Form15Request;
import com.bank.td.dto.request.MarkLienRequest;
import com.bank.td.dto.request.OpenDepositRequest;
import com.bank.td.dto.response.*;
import com.bank.td.service.TermDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/term-deposits")
@RequiredArgsConstructor
public class TermDepositController {

    private final TermDepositService termDepositService;

    @PostMapping("/open")
    public ResponseEntity<TermDepositResponse> openDeposit(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @Valid @RequestBody OpenDepositRequest request) {
        String key = idempotencyKeyHeader != null ? idempotencyKeyHeader : request.idempotencyKey();
        OpenDepositRequest effectiveReq = new OpenDepositRequest(
                request.customerId(),
                request.keycloakUserId(),
                request.debitAccountNumber(),
                request.depositType(),
                request.amount(),
                request.tenureMonths(),
                request.interestPayout(),
                request.autoRenewal(),
                request.nomineeName(),
                request.nomineeRelationship(),
                request.nomineeMinor(),
                key
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(termDepositService.openDeposit(effectiveReq));
    }

    @GetMapping("/{depositAccountNumber}")
    public ResponseEntity<TermDepositResponse> getDeposit(@PathVariable String depositAccountNumber) {
        return ResponseEntity.ok(termDepositService.getDeposit(depositAccountNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TermDepositResponse>> getCustomerDeposits(@PathVariable String customerId) {
        return ResponseEntity.ok(termDepositService.getCustomerDeposits(customerId));
    }

    @GetMapping("/{depositAccountNumber}/advice")
    public ResponseEntity<DepositAdviceResponse> getDepositAdvice(@PathVariable String depositAccountNumber) {
        return ResponseEntity.ok(termDepositService.getDepositAdvice(depositAccountNumber));
    }

    @PostMapping("/{depositAccountNumber}/trial-closure")
    public ResponseEntity<TrialClosureResponse> simulateTrialClosure(@PathVariable String depositAccountNumber) {
        return ResponseEntity.ok(termDepositService.simulateTrialClosure(depositAccountNumber));
    }

    @PostMapping("/close")
    public ResponseEntity<TrialClosureResponse> closeDeposit(@Valid @RequestBody CloseDepositRequest request) {
        return ResponseEntity.ok(termDepositService.closeDeposit(request));
    }

    @PostMapping("/{depositAccountNumber}/lien")
    public ResponseEntity<LienResponse> markLien(
            @PathVariable String depositAccountNumber,
            @Valid @RequestBody MarkLienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termDepositService.markLien(depositAccountNumber, request));
    }

    @DeleteMapping("/{depositAccountNumber}/lien/{lienId}")
    public ResponseEntity<Void> releaseLien(
            @PathVariable String depositAccountNumber,
            @PathVariable UUID lienId) {
        termDepositService.releaseLien(depositAccountNumber, lienId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tax/form-15")
    public ResponseEntity<Form15Response> submitForm15(@Valid @RequestBody Form15Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termDepositService.submitForm15(request));
    }

    @GetMapping("/tax/interest-certificate")
    public ResponseEntity<InterestCertificateResponse> getInterestCertificate(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "2026") String financialYear) {
        return ResponseEntity.ok(termDepositService.getInterestCertificate(customerId, financialYear));
    }

    @GetMapping("/tax/tds-certificate")
    public ResponseEntity<TDSCertificateResponse> getTdsCertificate(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "2026") String financialYear) {
        return ResponseEntity.ok(termDepositService.getTdsCertificate(customerId, financialYear));
    }
}
