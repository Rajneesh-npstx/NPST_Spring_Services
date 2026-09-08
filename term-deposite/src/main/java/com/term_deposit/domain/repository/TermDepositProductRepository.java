package com.term_deposit.domain.repository;

import com.term_deposit.domain.entity.TermDepositProduct;
import com.term_deposit.common.enums.ProductWorkingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermDepositProductRepository extends JpaRepository<TermDepositProduct, UUID> {
    Optional<TermDepositProduct> findByProductCode(String productCode);
    List<TermDepositProduct> findByWorkingStatus(ProductWorkingStatus status);
}