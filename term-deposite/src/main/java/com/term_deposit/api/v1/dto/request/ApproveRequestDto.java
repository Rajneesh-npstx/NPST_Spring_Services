package com.term_deposit.api.v1.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApproveRequestDto(
        @NotNull(message = "Checker User ID is required")
        UUID checkerUserId
) {}