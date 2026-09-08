package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsLoanInquiryResponse {

    @JsonProperty("CustomerId")
    private String customerId;

    @JsonProperty("Loans")
    private JsonNode loansNode;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CbsLoanItem> extractLoanList(ObjectMapper mapper) {
        List<CbsLoanItem> list = new ArrayList<>();
        if (loansNode == null || loansNode.isNull()) {
            return list;
        }
        try {
            if (loansNode.isArray()) {
                for (JsonNode itemNode : loansNode) {
                    list.add(mapper.treeToValue(itemNode, CbsLoanItem.class));
                }
            } else if (loansNode.isObject()) {
                JsonNode loanArr = loansNode.get("Loan");
                if (loanArr != null && loanArr.isArray()) {
                    for (JsonNode itemNode : loanArr) {
                        list.add(mapper.treeToValue(itemNode, CbsLoanItem.class));
                    }
                } else {
                    loansNode.fields().forEachRemaining(entry -> {
                        if (entry.getValue().isArray()) {
                            for (JsonNode itemNode : entry.getValue()) {
                                try {
                                    list.add(mapper.treeToValue(itemNode, CbsLoanItem.class));
                                } catch (Exception ignored) {}
                            }
                        }
                    });
                }
            }
        } catch (Exception ignored) {}
        return list;
    }
}
