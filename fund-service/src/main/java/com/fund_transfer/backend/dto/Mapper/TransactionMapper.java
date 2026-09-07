package com.fund_transfer.backend.dto.Mapper;

import com.fund_transfer.backend.dto.Response.TransactionResponse;
import com.fund_transfer.backend.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getInitiatorCif(),
                transaction.getDestinationAccountNumber(),
                transaction.getDestinationIfscCode(),
                transaction.getAmountMinorUnits(),
                transaction.getCurrency(),
                transaction.getTransferMode(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getInitiatedAt(),
                transaction.getCompletedAt()
        );
    }
}