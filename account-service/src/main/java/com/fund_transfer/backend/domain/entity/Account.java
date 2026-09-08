package com.fund_transfer.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

   @Id
   @Column(name = "account_no", length = 32)
   private String accountNo;              // CBS account number, natural key

   @Column(name = "cif_id", nullable = false, length = 20)
   private String cifId;

   @Column(name = "customer_name", nullable = false)
   private String customerName;           // denormalized for display

   @Column(name = "type", nullable = false, length = 40)
   private String type;                   // Savings (SB) / Current (CA) / FD / RD

   @Column(name = "branch", length = 60)
   private String branch;

   @Column(name = "ifsc", length = 15)
   private String ifsc;

   @Column(name = "balance", precision = 19, scale = 2)
   private BigDecimal balance;            // major units for now (pending money-units answer)

   @Enumerated(EnumType.STRING)
   @Column(name = "status", nullable = false, length = 20)
   private AccountStatus status;
   
   @Column(name = "opened_date")
   private LocalDate openedDate;
}