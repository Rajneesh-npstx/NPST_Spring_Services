package com.bank.loan.cbs;

import com.bank.loan.cbs.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CbsV3LoanClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CbsV3LoanClient(@Value("${cbs.v3.base-url:http://localhost:8000}") String cbsBaseUrl,
                           ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(cbsBaseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    public CbsLoanApplyResponse applyLoan(CbsLoanApplyRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v3/loans/apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CbsLoanApplyResponse.class);
        } catch (Exception e) {
            log.error("Failed to submit loan application to CBS v3: {}", e.getMessage());
            throw new RuntimeException("CBS v3 loan application failed: " + e.getMessage(), e);
        }
    }

    public List<CbsLoanItem> inquireLoans(String customerId) {
        try {
            CbsLoanInquiryResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v3/loans/accounts")
                            .queryParam("customerId", customerId)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CbsLoanInquiryResponse.class);

            if (response != null) {
                return response.extractLoanList(objectMapper);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch loans from CBS v3: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<CbsScheduleInstallment> fetchAmortizationSchedule(String loanAccountNumber) {
        try {
            CbsLoanScheduleResponse response = restClient.get()
                    .uri("/api/v3/loans/{loanId}/schedule", loanAccountNumber)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CbsLoanScheduleResponse.class);

            if (response != null) {
                return response.extractScheduleList(objectMapper);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch amortization schedule from CBS v3 for loan {}: {}", loanAccountNumber, e.getMessage());
        }
        return Collections.emptyList();
    }

    public CbsInterestCertResponse getInterestCertificate(String customerId, String financialYear) {
        try {
            return restClient.post()
                    .uri("/api/v3/certificates/interest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsInterestCertRequest(customerId, financialYear))
                    .retrieve()
                    .body(CbsInterestCertResponse.class);
        } catch (Exception e) {
            log.warn("Failed to fetch interest certificate from CBS v3: {}", e.getMessage());
            return null;
        }
    }
}
