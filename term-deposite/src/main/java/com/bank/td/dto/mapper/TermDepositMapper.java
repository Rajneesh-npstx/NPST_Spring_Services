package com.bank.td.dto.mapper;

import com.bank.td.dto.response.DepositAdviceResponse;
import com.bank.td.dto.response.TermDepositResponse;
import com.bank.td.entity.TermDeposit;
import org.springframework.stereotype.Component;

@Component
public class TermDepositMapper {

    public TermDepositResponse toResponse(TermDeposit td) {
        if (td == null) return null;
        return new TermDepositResponse(
                td.getId(),
                td.getDepositAccountNumber(),
                td.getCustomerId(),
                td.getKeycloakUserId(),
                td.getDepositType(),
                td.getInterestPayout(),
                td.getPrincipalAmount(),
                td.getInterestRate(),
                td.getMaturityAmount(),
                td.getDepositDate(),
                td.getMaturityDate(),
                td.getTenureMonths(),
                td.getDebitAccountNumber(),
                td.getAutoRenewal(),
                td.getStatus(),
                td.getNomineeName(),
                td.getNomineeRelationship(),
                td.getNomineeMinor(),
                td.getHasLien(),
                td.getLienReason(),
                td.getCreatedAt(),
                td.getUpdatedAt()
        );
    }

    public DepositAdviceResponse toAdvice(TermDeposit td) {
        if (td == null) return null;
        return new DepositAdviceResponse(
                "ADV-" + td.getDepositAccountNumber(),
                td.getDepositAccountNumber(),
                td.getCustomerId(),
                td.getDepositType(),
                td.getInterestPayout(),
                td.getPrincipalAmount(),
                td.getInterestRate(),
                td.getMaturityAmount(),
                td.getDepositDate(),
                td.getMaturityDate(),
                td.getTenureMonths(),
                td.getAutoRenewal(),
                td.getNomineeName(),
                td.getNomineeRelationship(),
                "Main Branch (001)",
                "This deposit receipt is electronically generated and adheres to RBI guidelines."
        );
    }
}
