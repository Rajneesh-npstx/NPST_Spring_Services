package com.term_deposit.domain.repository;

import com.term_deposit.domain.entity.TermDeposit;
import com.term_deposit.common.enums.DepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermDepositRepository extends JpaRepository<TermDeposit, UUID> {
    Optional<TermDeposit> findByDepositNumber(String depositNumber);
    List<TermDeposit> findByCif(String cif);
    List<TermDeposit> findByStatus(DepositStatus status);
}