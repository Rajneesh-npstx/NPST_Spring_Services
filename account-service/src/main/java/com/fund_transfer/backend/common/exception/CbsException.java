package com.fund_transfer.backend.common.exception;

import lombok.Getter;

@Getter
public class CbsException extends RuntimeException {

    private final String cbsCode;
    private final int httpStatus;

    public CbsException(String cbsCode, String message, int httpStatus) {
        super(message);
        this.cbsCode = cbsCode;
        this.httpStatus = httpStatus;
    }
}