package com.fund_transfer.backend.client.cbs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class CbsRestClientConfig {

    @Value("${cbs.base-url:http://localhost:9090/api/v1}")
    private String cbsBaseUrl;

    @Value("${cbs.channel-id:INTERNET_BANKING}")
    private String channelId;

    @Bean
    public RestClient cbsRestClient(RestClient.Builder builder) {

        var requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
                .requestFactory(requestFactory)
                .baseUrl(cbsBaseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_XML_VALUE)
                .defaultHeader("X-Channel-Id", channelId)
                .build();
    }
}