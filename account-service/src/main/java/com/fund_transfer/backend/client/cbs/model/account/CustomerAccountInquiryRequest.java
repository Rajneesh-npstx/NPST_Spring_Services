package com.fund_transfer.backend.client.cbs.model.account;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

@JacksonXmlRootElement(localName = "CustomerAccountInquiryRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountInquiryRequest {

    @JacksonXmlProperty(localName = "CustomerId")
    private String customerId;
}