package com.fund_transfer.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, UUID> {

    List<ScheduledTransfer> findByCif(String cif);

    List<ScheduledTransfer> findByStatusAndNextExecutionDateLessThanEqual(ScheduleStatus status, LocalDate date);
}