package com.fund_transfer.backend.dto.Response;

import com.fund_transfer.backend.enums.ScheduleFrequency;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.enums.TransactionStatus;
import com.fund_transfer.backend.enums.TransferMode;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ScheduledTransferResponse(
        Long id,
        String cif,
        Long beneficiaryId,
        BigInteger amountMinorUnits,
        TransferMode transferMode,
        ScheduleFrequency frequency,
        LocalDate nextExecutionDate,
        LocalDate endDate,
        ScheduleStatus status,
        ScheduleStatus lastExecutionStatus,
        Instant lastExecutedAt,
        Long retryCount,
        Long maxRetries,
        Instant createdAt,
        Instant updatedAt
) {
}
