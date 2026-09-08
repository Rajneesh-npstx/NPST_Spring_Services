package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Mapper.BeneficiaryMapper;
import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.repository.BeneficiaryRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepo repository;
    private final BeneficiaryMapper beneficiaryMapper;

    // Bank-configurable cooling-off window.
    // Zero disables cooling period.
    @Value("${funds-transfer.beneficiary.cooling-off-hours:24}")
    private long coolingOffHours;

    @Value("${funds-transfer.bank-code:DEFAULT}")
    private String bankCode;

    public BeneficiaryService(
            BeneficiaryRepo repository,
            BeneficiaryMapper beneficiaryMapper
    ) {
        this.repository = repository;
        this.beneficiaryMapper = beneficiaryMapper;
    }

    @Transactional
    public BeneficiaryResponse initiateAdd(
            CreateBeneficiaryRequest request
    ) throws Exception {

        // TODO: Add duplicate beneficiary validation here.
        //
         Example:
         if (repository.existsByOwnerCifAndBeneficiaryAccountNumberAndBeneficiaryIfscCode(
                 request.ownerCif(),
                 request.beneficiaryAccountNumber(),
                 request.beneficiaryIfscCode())) {
             throw new Exception(
                     "Beneficiary already exists for this account and IFSC"
             );
         }

        /*
         * Determine beneficiary status and cooling period.
         */
        BeneficiaryStatus status;
        Instant coolingPeriodEndsAt = null;

        if (coolingOffHours > 0) {
            status = BeneficiaryStatus.PENDING_COOLING_PERIOD;
            coolingPeriodEndsAt =
                    Instant.now().plusSeconds(coolingOffHours * 60 * 60);
        } else {
            status = BeneficiaryStatus.ACTIVE;
        }

        /*
         * Create beneficiary.
         */
        Beneficiary beneficiary = Beneficiary.builder()
                .ownerCif(request.ownerCif())
                .ownerKeycloakUserId(request.ownerKeycloakUserId())
                .beneficiaryName(request.beneficiaryName())
                .beneficiaryAccountNumber(request.beneficiaryAccountNumber())
                .beneficiaryIfscCode(request.beneficiaryIfscCode())
                .nickname(request.nickname())
                .transferMode(request.transferMode())
                .status(status)
                .type(request.type())
                .build();

        /*
         * Save beneficiary.
         */
        Beneficiary savedBeneficiary = repository.save(beneficiary);

        /*
         * Convert entity to response DTO.
         */
        return beneficiaryMapper.toResponse(savedBeneficiary);
    }
}