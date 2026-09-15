package com.example.cardsdbccp.repository;

import com.example.cardsdbccp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Customer findByLastNameIgnoreCaseAndEmailIgnoreCase(String lastName, String email);

    Customer findByLastNameIgnoreCaseAndMobile(String lastName, Long mobile);

    Customer findByEmailIgnoreCase(String email);

    Customer findByMobile(Long mobile);
}
