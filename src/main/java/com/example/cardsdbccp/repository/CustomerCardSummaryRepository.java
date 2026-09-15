package com.example.cardsdbccp.repository;

import com.example.cardsdbccp.model.CustomerCardSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerCardSummaryRepository extends JpaRepository<CustomerCardSummary, UUID> {

    Optional<CustomerCardSummary> findByCustomer_CustomerId(UUID customerId);

    Optional<CustomerCardSummary> findByCustomer_Email(String email);
}
