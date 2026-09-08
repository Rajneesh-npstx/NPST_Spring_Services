package com.fund_transfer.backend.controller;


import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Beneficiary Management & Lookup endpoints (PRD 6.4 / US-09).
 *
 * ownerCustomerId is read from the authenticated principal (resolved by
 * Auth Service via the API Gateway) — shown here as a header param for
 * clarity; swap for @AuthenticationPrincipal once security config is wired.
 *
 * Permission mapping note: §4.1 of the RBAC spec only defines
 * beneficiary:create and beneficiary:view — there's no separate
 * beneficiary:delete / beneficiary:block permission. Mutating-but-not-add
 * actions (remove, block) are mapped onto beneficiary:create here since
 * that's the closest existing write permission; see PermissionService
 * javadoc.
 */
@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
//    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(
            @Valid @RequestBody CreateBeneficiaryRequest request) throws Exception {
        BeneficiaryResponse response = beneficiaryService.initiateAdd(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
