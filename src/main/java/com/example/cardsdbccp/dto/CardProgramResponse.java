package com.example.cardsdbccp.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record CardProgramResponse(
        UUID programId,
        String name,
        BigDecimal interestRate,
        String airLoyaltyPlan,
        Integer interestFreeDays,
        BigInteger maxCreditLimit,
        BigInteger minCreditLimit) {}
