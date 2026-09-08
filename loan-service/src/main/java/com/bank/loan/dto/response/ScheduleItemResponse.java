package com.bank.loan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleItemResponse(
        Integer installmentNo,
        LocalDate dueDate,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal totalInstallment,
        BigDecimal endingBalance
) {}
