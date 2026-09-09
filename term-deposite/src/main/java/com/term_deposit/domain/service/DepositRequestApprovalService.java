package com.term_deposit.domain.service;

import com.term_deposit.common.enums.DepositRequestStatus;
import com.term_deposit.common.exception.BusinessRuleViolationException;
import com.term_deposit.common.exception.ResourceNotFoundException;
import com.term_deposit.domain.entity.DepositRequest;
import com.term_deposit.domain.repository.DepositRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositRequestApprovalService {

    private final DepositRequestRepository requestRepository;
    private final TermDepositService termDepositService;

    @Transactional
    public void approveRequest(UUID requestId, UUID checkerUserId) {
        log.info("Attempting to approve request ID: {} by user: {}", requestId, checkerUserId);

        // 1. Fetch the request
        DepositRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit Request not found: " + requestId));

        // 2. Validate state machine (Only UNDER_REVIEW can be approved)
        if (request.getStatus() != DepositRequestStatus.UNDER_REVIEW) {
            throw new BusinessRuleViolationException("Only requests UNDER_REVIEW can be approved. Current status: " + request.getStatus());
        }

        // 3. Segregation of Duties (Maker-Checker Rule)
        // If a branch teller made the request, they cannot use their manager's terminal to approve it
        if (request.getMakerKeycloakUserId().equals(checkerUserId)) {
            throw new BusinessRuleViolationException("Segregation of duties violation: Maker cannot approve their own request.");
        }

        // 4. Update the request status
        request.setStatus(DepositRequestStatus.APPROVED);
        request.setCheckerKeycloakUserId(checkerUserId);
        request.setReviewedAt(Instant.now());

        requestRepository.save(request);

        // 5. Route to the correct execution logic based on the type of request
        switch (request.getRequestType()) {
            case CREATE_FIXED_DEPOSIT:
            case CREATE_RECURRING_DEPOSIT:
                termDepositService.executeApprovedDepositCreation(request);
                break;
            case PREMATURE_CLOSURE:
                // termDepositService.executePrematureClosure(request);
                log.info("Premature closure routed for request: {}", request.getRequestReference());
                break;
            default:
                log.warn("Unhandled request type approved: {}", request.getRequestType());
        }
    }
}