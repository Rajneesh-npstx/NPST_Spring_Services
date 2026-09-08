package com.fund_transfer.backend.domain.repository;

import com.fund_transfer.backend.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
   List<Account> findByCifId(String cifId);
}
