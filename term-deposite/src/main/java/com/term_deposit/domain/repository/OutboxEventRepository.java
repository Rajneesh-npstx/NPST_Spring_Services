package com.term_deposit.domain.repository;

import com.term_deposit.domain.entity.OutboxEvent;
import com.term_deposit.common.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    // This allows our background scheduler to grab the oldest unprocessed events first
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}