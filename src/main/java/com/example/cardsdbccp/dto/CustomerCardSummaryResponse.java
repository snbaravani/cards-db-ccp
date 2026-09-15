package com.example.cardsdbccp.dto;

import com.example.cardsdbccp.model.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerCardSummaryResponse(
        UUID customerId,
        UUID programId,
        String programmeName,
        CardStatus cardStatus,
        Boolean fraudFlag,
        LocalDate expiryDate,
        BigDecimal creditOutstanding) {}
