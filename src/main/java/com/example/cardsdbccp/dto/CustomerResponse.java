package com.example.cardsdbccp.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID customerId, String firstName, String lastName, String email, Long mobile, LocalDate dob) {}
