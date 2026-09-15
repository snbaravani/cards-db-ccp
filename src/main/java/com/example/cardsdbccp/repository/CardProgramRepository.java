package com.example.cardsdbccp.repository;

import com.example.cardsdbccp.model.CardProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardProgramRepository extends JpaRepository<CardProgram, UUID> {

    List<CardProgram> findByNameIgnoreCase(String name);
}
