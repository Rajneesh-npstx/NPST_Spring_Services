package com.fund_transfer.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fund_transfer.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findByInitiatorCifOrderByInitiatedAtDesc(String initiatorCif);
}