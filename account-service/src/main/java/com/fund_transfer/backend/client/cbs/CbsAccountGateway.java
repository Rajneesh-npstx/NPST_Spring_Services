package com.fund_transfer.backend.client.cbs;

import com.fund_transfer.backend.domain.entity.Account;

import java.util.List;

public interface CbsAccountGateway {

    /** All accounts held by a customer (maps to CBS Customer Account Inquiry). */
    List<Account> getAccountsByCif(String cifId);

    // Pending manager's scope answer — uncomment when confirmed:
    // Account getAccountDetails(String accountNo);
    // void freeze(String accountNo, String freezeType, String reasonCode);
    // void unfreeze(String accountNo);
}