package com.bank.loan.repository;

import com.bank.loan.entity.LoanAccount;
import com.bank.loan.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, UUID> {

    Optional<LoanAccount> findByLoanAccountNumber(String loanAccountNumber);

    Optional<LoanAccount> findByIdempotencyKey(String idempotencyKey);

    List<LoanAccount> findByCustomerId(String customerId);

    List<LoanAccount> findByCustomerIdAndStatus(String customerId, LoanStatus status);
}
