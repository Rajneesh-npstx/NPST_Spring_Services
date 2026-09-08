package com.bank.td.cbs;

import com.bank.td.cbs.dto.*;
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
public class CbsV3Client {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CbsV3Client(@Value("${cbs.v3.base-url:http://localhost:8000}") String cbsBaseUrl,
                       ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(cbsBaseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    public CbsOpenDepositResponse openTermDeposit(CbsOpenDepositRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v3/deposits/td/open")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CbsOpenDepositResponse.class);
        } catch (Exception e) {
            log.error("Failed to open term deposit in CBS v3: {}", e.getMessage());
            throw new RuntimeException("CBS v3 deposit creation failed: " + e.getMessage(), e);
        }
    }

    public List<CbsDepositItem> inquireDeposits(String customerId) {
        try {
            CbsDepositInquiryResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v3/deposits/accounts")
                            .queryParam("customerId", customerId)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CbsDepositInquiryResponse.class);

            if (response != null) {
                return response.extractDepositList(objectMapper);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch term deposits from CBS v3: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    public CbsTrialClosureResponse simulateTrialClosure(String tdAccountId) {
        try {
            return restClient.post()
                    .uri("/api/v3/deposits/td/trial-closure")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsTrialClosureRequest(tdAccountId))
                    .retrieve()
                    .body(CbsTrialClosureResponse.class);
        } catch (Exception e) {
            log.error("Failed to simulate trial closure in CBS v3: {}", e.getMessage());
            throw new RuntimeException("CBS v3 trial closure failed: " + e.getMessage(), e);
        }
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
            log.error("Failed to fetch interest certificate from CBS v3: {}", e.getMessage());
            throw new RuntimeException("CBS v3 interest certificate inquiry failed: " + e.getMessage(), e);
        }
    }

    public CbsTdsCertResponse getTdsCertificate(String customerId, String financialYear) {
        try {
            return restClient.post()
                    .uri("/api/v3/certificates/tds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CbsTdsCertRequest(customerId, financialYear))
                    .retrieve()
                    .body(CbsTdsCertResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch TDS certificate from CBS v3: {}", e.getMessage());
            throw new RuntimeException("CBS v3 TDS certificate inquiry failed: " + e.getMessage(), e);
        }
    }
}
