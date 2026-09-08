package com.fund_transfer.backend.client.cbs.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbsRequest<T> {

    @JacksonXmlProperty(localName = "Header")
    private CbsHeader header;

    @JacksonXmlProperty(localName = "Body")
    private T body;
}