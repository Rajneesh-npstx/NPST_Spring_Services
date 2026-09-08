package com.fund_transfer.backend.client.cbs.model.account;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbsAccount {

    @JacksonXmlProperty(localName = "AccountNumber")
    private String accountNumber;

    @JacksonXmlProperty(localName = "AccountType")
    private String accountType;

    @JacksonXmlProperty(localName = "Currency")
    private String currency;

    @JacksonXmlProperty(localName = "LedgerBalance")
    private BigDecimal ledgerBalance;

    @JacksonXmlProperty(localName = "AvailableBalance")
    private BigDecimal availableBalance;

    @JacksonXmlProperty(localName = "Status")
    private String status;
}