package com.bank.loan.repository;

import com.bank.loan.entity.DisbursementTranche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisbursementTrancheRepository extends JpaRepository<DisbursementTranche, UUID> {

    List<DisbursementTranche> findByLoanAccountNumberOrderByTrancheNumberAsc(String loanAccountNumber);
}
