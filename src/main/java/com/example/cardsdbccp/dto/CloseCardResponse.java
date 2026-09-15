package com.example.cardsdbccp.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CloseCardResponse(
        UUID customerId, String response, boolean isError, boolean isRetryable) {}
