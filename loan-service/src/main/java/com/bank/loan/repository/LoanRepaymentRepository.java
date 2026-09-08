package com.bank.loan.repository;

import com.bank.loan.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {

    List<LoanRepayment> findByLoanAccountNumberOrderByPaymentDateDesc(String loanAccountNumber);

    @Query("SELECT r FROM LoanRepayment r WHERE r.loanAccountNumber = :loanAccountNumber AND r.paymentDate BETWEEN :startDate AND :endDate")
    List<LoanRepayment> findRepaymentsInFinancialYear(
            @Param("loanAccountNumber") String loanAccountNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
