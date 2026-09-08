package com.bank.td.cbs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsDepositInquiryResponse {

    @JsonProperty("CustomerId")
    private String customerId;

    @JsonProperty("Deposits")
    private JsonNode depositsNode;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CbsDepositItem> extractDepositList(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        List<CbsDepositItem> list = new ArrayList<>();
        if (depositsNode == null || depositsNode.isNull()) {
            return list;
        }
        try {
            if (depositsNode.isArray()) {
                for (JsonNode itemNode : depositsNode) {
                    list.add(mapper.treeToValue(itemNode, CbsDepositItem.class));
                }
            } else if (depositsNode.isObject()) {
                // Check if nested under "Deposit"
                JsonNode depositArr = depositsNode.get("Deposit");
                if (depositArr != null && depositArr.isArray()) {
                    for (JsonNode itemNode : depositArr) {
                        list.add(mapper.treeToValue(itemNode, CbsDepositItem.class));
                    }
                } else {
                    // Check all child values
                    depositsNode.fields().forEachRemaining(entry -> {
                        if (entry.getValue().isArray()) {
                            for (JsonNode itemNode : entry.getValue()) {
                                try {
                                    list.add(mapper.treeToValue(itemNode, CbsDepositItem.class));
                                } catch (Exception ignored) {}
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return list;
    }
}
