package com.fund_transfer.backend.api.v1.controller;

import com.fund_transfer.backend.api.v1.dto.response.AccountResponse;
import com.fund_transfer.backend.api.v1.mapper.AccountMapper;
import com.fund_transfer.backend.domain.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

   private final AccountService accountService;
   private final AccountMapper accountMapper;
   
   @GetMapping
   public List<AccountResponse> list(@RequestParam(required = false) String cifId) {
       var accounts = (cifId == null || cifId.isBlank())
               ? accountService.listAccounts()
               : accountService.listAccountsByCif(cifId);
       return accountMapper.toResponseList(accounts);
   }
}
