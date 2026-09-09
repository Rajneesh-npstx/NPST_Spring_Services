package com.term_deposit.api.v1.controller;

import com.term_deposit.api.v1.dto.deposit.OpenDepositRequest;
import com.term_deposit.api.v1.dto.request.ApproveRequestDto;
import com.term_deposit.common.enums.DepositRequestStatus;
import com.term_deposit.common.enums.DepositRequestType;
import com.term_deposit.domain.entity.DepositRequest;
import com.term_deposit.domain.repository.DepositRequestRepository;
import com.term_deposit.domain.service.DepositRequestApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/term-deposits")
@RequiredArgsConstructor
public class TermDepositController {

    private final DepositRequestApprovalService approvalService;
    private final DepositRequestRepository depositRequestRepository; // In a production app, this delegates to a MakerService

    /**
     * MAKER ENDPOINT: Initiates the request.
     * Does NOT move money. Saves to database as UNDER_REVIEW.
     */
    @PostMapping("/requests")
    public ResponseEntity<String> initiateDepositRequest(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OpenDepositRequest requestDto) {

        log.info("Received request to open FD for CIF: {}", requestDto.cif());

        // Save as a pending request for the Checker to review
        DepositRequest pendingRequest = DepositRequest.builder()
                .cif(requestDto.cif())
                .makerKeycloakUserId(requestDto.makerUserId())
                .requestType(DepositRequestType.CREATE_FIXED_DEPOSIT)
                .status(DepositRequestStatus.UNDER_REVIEW)
                // We store the raw JSON payload in 'remarks' for now so the Checker has the data
                .remarks("Amount: " + requestDto.principalAmount() + ", Tenure: " + requestDto.tenureMonths())
                .build();

        depositRequestRepository.save(pendingRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Request submitted successfully. Pending Checker approval. Reference ID: " + pendingRequest.getId());
    }

    /**
     * CHECKER ENDPOINT: Approves the request.
     * Triggers CBS integration and finalizes the Term Deposit.
     */
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<String> approveDepositRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ApproveRequestDto approveDto) {

        log.info("Received approval submission for Request ID: {}", requestId);

        // This routes into your segregated Service layer logic
        approvalService.approveRequest(requestId, approveDto.checkerUserId());

        return ResponseEntity.ok("Deposit Request approved and FD successfully created in Core Banking System.");
    }
}