package com.fund_transfer.backend.client.cbs.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbsError {

    @JacksonXmlProperty(localName = "ErrorCode")
    private String errorCode;

    @JacksonXmlProperty(localName = "ErrorDescription")
    private String errorDescription;
}