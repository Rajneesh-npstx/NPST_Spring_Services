package com.fund_transfer.backend.client.cbs;

import com.fund_transfer.backend.client.cbs.model.CbsError;
import com.fund_transfer.backend.common.exception.CbsException;

import java.util.Map;

public final class CbsErrorMapper {

    private CbsErrorMapper() {}

    private static final Map<String, Integer> STATUS = Map.ofEntries(
            Map.entry("BCB-001", 404),
            Map.entry("BCB-002", 404),
            Map.entry("BCB-003", 400),
            Map.entry("BCB-004", 400),
            Map.entry("BCB-005", 422),
            Map.entry("BCB-006", 422),
            Map.entry("BCB-017", 422),
            Map.entry("BCB-018", 403),
            Map.entry("BCB-019", 500),
            Map.entry("BCB-020", 504)
    );

    public static CbsException toException(CbsError error) {

        int status = STATUS.getOrDefault(
                error.getErrorCode(),
                502
        );

        return new CbsException(
                error.getErrorCode(),
                error.getErrorDescription(),
                status
        );
    }
}