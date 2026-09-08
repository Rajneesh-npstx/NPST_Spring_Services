package com.fund_transfer.backend.api.v1.mapper;

import com.fund_transfer.backend.api.v1.dto.response.AccountResponse;
import com.fund_transfer.backend.domain.entity.Account;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
   AccountResponse toResponse(Account account);      // status enum -> String is automatic
   List<AccountResponse> toResponseList(List<Account> accounts);
}
