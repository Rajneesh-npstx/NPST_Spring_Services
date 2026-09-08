package com.bank.td.repository;

import com.bank.td.entity.TermDeposit;
import com.bank.td.enums.DepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermDepositRepository extends JpaRepository<TermDeposit, UUID> {

    Optional<TermDeposit> findByDepositAccountNumber(String depositAccountNumber);

    Optional<TermDeposit> findByIdempotencyKey(String idempotencyKey);

    List<TermDeposit> findByCustomerIdOrderByDepositDateDesc(String customerId);

    List<TermDeposit> findByCustomerIdAndStatus(String customerId, DepositStatus status);
}
