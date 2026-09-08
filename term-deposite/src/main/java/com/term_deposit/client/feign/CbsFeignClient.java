package com.term_deposit.client.feign;

import com.term_deposit.client.feign.dto.CbsOpenTdRequest;
import com.term_deposit.client.feign.dto.CbsOpenTdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cbs-client", url = "${cbs.base-url:http://10.2.1.166:8000}")
public interface CbsFeignClient {

    @PostMapping("/api/v3/deposits/td/open")
    CbsOpenTdResponse openTermDeposit(@RequestBody CbsOpenTdRequest request);

}