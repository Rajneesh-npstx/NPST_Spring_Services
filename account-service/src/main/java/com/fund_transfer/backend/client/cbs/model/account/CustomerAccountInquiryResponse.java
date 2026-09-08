package com.fund_transfer.backend.client.cbs.model.account;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

import java.util.List;

@JacksonXmlRootElement(localName = "CustomerAccountInquiryResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountInquiryResponse {

    @JacksonXmlProperty(localName = "Customer")
    private Customer customer;

    @JacksonXmlElementWrapper(localName = "Accounts")
    @JacksonXmlProperty(localName = "Account")
    private List<CbsAccount> accounts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Customer {

        @JacksonXmlProperty(localName = "CustomerId")
        private String customerId;

        @JacksonXmlProperty(localName = "CustomerName")
        private String customerName;

        @JacksonXmlProperty(localName = "MobileNumber")
        private String mobileNumber;

        @JacksonXmlProperty(localName = "EmailId")
        private String emailId;

        @JacksonXmlProperty(localName = "KycStatus")
        private String kycStatus;
    }
}