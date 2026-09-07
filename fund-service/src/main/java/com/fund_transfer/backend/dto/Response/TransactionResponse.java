package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.enums.TransferMode;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String transactionReference,
        String cbsReferenceNumber,
        String idempotencyKey,
        String initiatorCif,
        UUID initiatorKeycloakUserId,
        UUID beneficiaryId,
        String destinationAccountNumber,
        String destinationIfscCode,
        BigInteger amountMinorUnits,
        String currency,
        TransferMode transferMode,
        TransactionStatus status,
        String failureReason,
        String remarks,
        String bankCode,
        Long version,
        Instant initiatedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {}