package com.bank.td.repository;

import com.bank.td.entity.LienRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LienRecordRepository extends JpaRepository<LienRecord, UUID> {

    List<LienRecord> findByDepositAccountNumberAndActiveTrue(String depositAccountNumber);

    Optional<LienRecord> findByLienReference(String lienReference);
}
