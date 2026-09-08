package com.bank.td.repository;

import com.bank.td.entity.Form15Submission;
import com.bank.td.enums.FormStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Form15SubmissionRepository extends JpaRepository<Form15Submission, UUID> {

    List<Form15Submission> findByCustomerIdOrderBySubmittedAtDesc(String customerId);

    Optional<Form15Submission> findByCustomerIdAndFinancialYearAndStatus(
            String customerId, String financialYear, FormStatus status);
}
