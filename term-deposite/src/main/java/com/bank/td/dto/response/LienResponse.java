package com.bank.td.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LienResponse(
        UUID id,
        String depositAccountNumber,
        String lienReference,
        BigDecimal lienAmount,
        String reason,
        Boolean active,
        Instant markedAt,
        Instant releasedAt
) {}
