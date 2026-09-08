package com.fund_transfer.backend.repository;

import java.util.List;
import java.util.UUID;

import com.fund_transfer.backend.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepo extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByOwnerCif(String ownerCif);

    boolean existsByOwnerCifAndBeneficiaryAccountNumberAndBeneficiaryIfscCode(String ownerCif, String beneficiaryAccountNumber, String beneficiaryIfscCode);
}