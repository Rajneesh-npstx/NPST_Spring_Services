package com.fund_transfer.backend.client.cbs;

import com.fund_transfer.backend.client.cbs.model.*;
import com.fund_transfer.backend.client.cbs.model.account.*;
import com.fund_transfer.backend.common.exception.CbsException;
import com.fund_transfer.backend.domain.entity.Account;
import com.fund_transfer.backend.domain.entity.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CbsAccountGatewayImpl implements CbsAccountGateway {

    private final RestClient cbsRestClient;

    @Value("${cbs.user-id:APIUSER}")
    private String userId;

    @Value("${cbs.branch-code:001}")
    private String branchCode;

    @Value("${cbs.channel-id:INTERNET_BANKING}")
    private String channelId;

    @Override
    public List<Account> getAccountsByCif(String cifId) {

        var body = CustomerAccountInquiryRequest.builder().customerId(cifId).build();

        var request = CbsRequest.<CustomerAccountInquiryRequest>builder()
                .header(buildHeader())
                .body(body)
                .build();

        CbsResponse<CustomerAccountInquiryResponse> response = cbsRestClient.post()
                .uri("/customers/accounts/inquiry")
                .header("X-Correlation-Id", correlationId())
                .body(request)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        if (response == null || response.getBody() == null) {
            throw new CbsException("BCB-019", "Empty response from CBS", 502);
        }

        var payload = response.getBody();
        return payload.getAccounts().stream()
                .map(a -> toAccount(a, payload.getCustomer()))
                .toList();
    }

    private Account toAccount(CbsAccount a, CustomerAccountInquiryResponse.Customer c) {
        return Account.builder()
                .accountNo(a.getAccountNumber())
                .cifId(c.getCustomerId())
                .customerName(c.getCustomerName())
                .type(a.getAccountType())
                .balance(a.getAvailableBalance())
                .status(mapStatus(a.getStatus()))
                .build();
    }

    private AccountStatus mapStatus(String cbsStatus) {
        if (cbsStatus == null) return AccountStatus.ACTIVE;
        try {
            return AccountStatus.valueOf(cbsStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AccountStatus.ACTIVE;
        }
    }

    private CbsHeader buildHeader() {
        return CbsHeader.builder()
                .messageId("MSG" + System.currentTimeMillis())
                .correlationId(correlationId())
                .channelId(channelId)
                .userId(userId)
                .branchCode(branchCode)
                .requestTimestamp(Instant.now().toString())
                .build();
    }

    private String correlationId() {
        return "CORR-" + UUID.randomUUID();
    }
}