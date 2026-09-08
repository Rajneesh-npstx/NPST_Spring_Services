package com.fund_transfer.backend.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice

public class GlobalExceptionHandler {
   @ExceptionHandler(CbsException.class)
   public ResponseEntity<Map<String, String>> handleCbs(CbsException ex) {
       String msg = ex.getMessage() == null ? "CBS error" : ex.getMessage();
       return ResponseEntity.status(ex.getHttpStatus())
               .body(Map.of("message", msg, "code", ex.getCbsCode()));
   }
}
