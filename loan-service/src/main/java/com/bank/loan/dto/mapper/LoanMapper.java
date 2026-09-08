package com.bank.loan.dto.mapper;

import com.bank.loan.dto.response.LoanAccountResponse;
import com.bank.loan.dto.response.LoanApplicationResponse;
import com.bank.loan.entity.LoanAccount;
import com.bank.loan.entity.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanAccountResponse toResponse(LoanAccount loan) {
        if (loan == null) return null;
        return new LoanAccountResponse(
                loan.getId(),
                loan.getLoanAccountNumber(),
                loan.getCustomerId(),
                loan.getKeycloakUserId(),
                loan.getLoanType(),
                loan.getSanctionedAmount(),
                loan.getDisbursedAmount(),
                loan.getOutstandingPrincipal(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getRemainingTenureMonths(),
                loan.getEmiAmount(),
                loan.getNextDueDate(),
                loan.getStatus(),
                loan.getInterestType(),
                loan.getBranchCode(),
                loan.getLinkedDebitAccount(),
                loan.getClosedAt(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }

    public LoanApplicationResponse toApplicationResponse(LoanApplication app, String message) {
        if (app == null) return null;
        return new LoanApplicationResponse(
                app.getId(),
                app.getApplicationNumber(),
                app.getCustomerId(),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getTenureMonths(),
                app.getMonthlyIncome(),
                app.getBranchCode(),
                app.getStatus(),
                app.getEstimatedEmi(),
                message,
                app.getCreatedAt()
        );
    }
}
