package com.fund_transfer.backend.domain.service;

import com.fund_transfer.backend.client.cbs.CbsAccountGateway;
import com.fund_transfer.backend.domain.entity.Account;
import com.fund_transfer.backend.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

   private final AccountRepository accountRepository;
   private final CbsAccountGateway cbsAccountGateway;   // Prateek implements this

   @Transactional(readOnly = true)
   public List<Account> listAccounts() {
       return accountRepository.findAll();
   }
   @Transactional(readOnly = true)
   public List<Account> listAccountsByCif(String cifId) {
       return accountRepository.findByCifId(cifId);
   }
   
   /** Pull straight from CBS (bypasses local DB) — used when live balances are needed. */
   @Transactional(readOnly = true)
   public List<Account> fetchFromCbs(String cifId) {
       return cbsAccountGateway.getAccountsByCif(cifId);
   }
}