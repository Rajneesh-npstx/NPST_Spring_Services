package com.term_deposit.domain.service;

import com.term_deposit.client.feign.CbsFeignClient;
import com.term_deposit.client.feign.dto.CbsOpenTdRequest;
import com.term_deposit.client.feign.dto.CbsOpenTdResponse;
import com.term_deposit.common.enums.DepositStatus;
import com.term_deposit.common.enums.OutboxStatus;
import com.term_deposit.domain.entity.DepositRequest;
import com.term_deposit.domain.entity.OutboxEvent;
import com.term_deposit.domain.entity.TermDeposit;
import com.term_deposit.domain.repository.OutboxEventRepository;
import com.term_deposit.domain.repository.TermDepositRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermDepositService {

    private final TermDepositRepository termDepositRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CbsFeignClient cbsFeignClient;

    @Transactional
    public TermDeposit executeApprovedDepositCreation(DepositRequest request) {
        log.info("Executing CBS FD Creation for Request Reference: {}", request.getRequestReference());

        // 1. Map local request to CBS external format
        // Note: In production, principal and tenure are parsed from a JSON payload stored in the DepositRequest
        CbsOpenTdRequest cbsRequest = new CbsOpenTdRequest(
                request.getCif(),
                "FD-REGULAR",
                new BigDecimal("50000.00"),
                12,
                "SAVINGS-12345",
                "ON_MATURITY"
        );

        // 2. Call the Core Banking System (This actually moves the money upstream)
        CbsOpenTdResponse cbsResponse = cbsFeignClient.openTermDeposit(cbsRequest);

        // 3. Create the permanent local Term Deposit record
        TermDeposit termDeposit = TermDeposit.builder()
                .depositNumber(cbsResponse.cbsReferenceNumber())
                .cif(request.getCif())
                .keycloakUserId(request.getKeycloakUserId())
                .status(DepositStatus.ACTIVE)
                // We snapshot the exact rate given by the CBS today
                .interestRate(cbsResponse.appliedInterestRate())
                .maturityDate(cbsResponse.maturityDate())
                .cbsReferenceNumber(cbsResponse.cbsReferenceNumber())
                .build();

        TermDeposit savedDeposit = termDepositRepository.save(termDeposit);

        // 4. Trigger Outbox Event for Asynchronous SMS/Email Notification
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("TERM_DEPOSIT")
                .aggregateId(savedDeposit.getId())
                .eventType("FD_CREATED_SUCCESS")
                .payload("{\"depositNumber\":\"" + savedDeposit.getDepositNumber() + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        outboxEventRepository.save(event);

        log.info("Successfully created Term Deposit: {}", savedDeposit.getDepositNumber());
        return savedDeposit;
    }
}