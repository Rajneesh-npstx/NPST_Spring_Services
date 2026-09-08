package com.bank.loan.repository;

import com.bank.loan.entity.AutoPayMandate;
import com.bank.loan.enums.MandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutoPayMandateRepository extends JpaRepository<AutoPayMandate, UUID> {

    Optional<AutoPayMandate> findByLoanAccountNumberAndStatus(String loanAccountNumber, MandateStatus status);

    Optional<AutoPayMandate> findByMandateReference(String mandateReference);

    List<AutoPayMandate> findByCustomerId(String customerId);
}
