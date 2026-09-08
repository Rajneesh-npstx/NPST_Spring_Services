package com.bank.loan.cbs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsLoanScheduleResponse {

    @JsonProperty("LoanAccountNumber")
    private String loanAccountNumber;

    @JsonProperty("InterestRate")
    private BigDecimal interestRate;

    @JsonProperty("Schedule")
    private JsonNode scheduleNode;

    public String getLoanAccountNumber() {
        return loanAccountNumber;
    }

    public void setLoanAccountNumber(String loanAccountNumber) {
        this.loanAccountNumber = loanAccountNumber;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public List<CbsScheduleInstallment> extractScheduleList(ObjectMapper mapper) {
        List<CbsScheduleInstallment> list = new ArrayList<>();
        if (scheduleNode == null || scheduleNode.isNull()) {
            return list;
        }
        try {
            if (scheduleNode.isArray()) {
                for (JsonNode itemNode : scheduleNode) {
                    list.add(mapper.treeToValue(itemNode, CbsScheduleInstallment.class));
                }
            } else if (scheduleNode.isObject()) {
                JsonNode instArr = scheduleNode.get("Installment");
                if (instArr != null && instArr.isArray()) {
                    for (JsonNode itemNode : instArr) {
                        list.add(mapper.treeToValue(itemNode, CbsScheduleInstallment.class));
                    }
                } else {
                    scheduleNode.fields().forEachRemaining(entry -> {
                        if (entry.getValue().isArray()) {
                            for (JsonNode itemNode : entry.getValue()) {
                                try {
                                    list.add(mapper.treeToValue(itemNode, CbsScheduleInstallment.class));
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
