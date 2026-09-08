package com.fund_transfer.backend.client.cbs.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbsHeader {

    @JacksonXmlProperty(localName = "MessageId")
    private String messageId;

    @JacksonXmlProperty(localName = "CorrelationId")
    private String correlationId;

    @JacksonXmlProperty(localName = "ChannelId")
    private String channelId;

    @JacksonXmlProperty(localName = "UserId")
    private String userId;

    @JacksonXmlProperty(localName = "BranchCode")
    private String branchCode;

    @JacksonXmlProperty(localName = "RequestTimestamp")
    private String requestTimestamp;
}