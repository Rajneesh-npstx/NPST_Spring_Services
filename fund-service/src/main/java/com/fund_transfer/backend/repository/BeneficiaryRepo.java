package com.fund_transfer.backend.repository;

import java.util.List;
import java.util.UUID;

import com.fund_transfer.backend.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepo extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByOwnerCif(String ownerCif);
}