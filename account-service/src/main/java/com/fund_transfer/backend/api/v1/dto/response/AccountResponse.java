package com.fund_transfer.backend.api.v1.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {
   private String accountNo;
   private String cifId;
   private String customerName;
   private String type;
   private String branch;
   private BigDecimal balance;
   private String status;
   private LocalDate openedDate;
   private String ifsc;
}
