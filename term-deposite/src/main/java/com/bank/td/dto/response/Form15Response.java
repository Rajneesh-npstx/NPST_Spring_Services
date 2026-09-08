package com.bank.td.dto.response;

import com.bank.td.enums.FormStatus;
import com.bank.td.enums.FormType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Form15Response(
        UUID id,
        String customerId,
        String pan,
        FormType formType,
        Integer customerAge,
        String financialYear,
        BigDecimal estimatedTotalIncome,
        FormStatus status,
        String rejectionReason,
        Instant submittedAt
) {}
