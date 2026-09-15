package com.example.cardsdbccp.model;

import com.example.cardsdbccp.util.AssertUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name = "card_program")
public class CardProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "program_id", updatable = false, nullable = false)
    private UUID programId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "air_loyalty_plan")
    private String airLoyaltyPlan;

    @Column(name = "interest_free_days")
    private Integer interestFreeDays;

    @Column(name = "max_credit_limit", nullable = false, precision = 30, scale = 0)
    private BigInteger maxCreditLimit;

    @Column(name = "min_credit_limit", nullable = false, precision = 30, scale = 0)
    private BigInteger minCreditLimit;

    protected CardProgram() {
        // required by JPA
    }

    public CardProgram(
            String name,
            BigDecimal interestRate,
            String airLoyaltyPlan,
            Integer interestFreeDays,
            BigInteger maxCreditLimit,
            BigInteger minCreditLimit) {
        this.name = AssertUtil.requireNotBlank(name, "Name cannot be null or empty");
        this.interestRate = AssertUtil.requireNotNull(interestRate, "Interest rate cannot be null");
        this.airLoyaltyPlan = airLoyaltyPlan;
        this.interestFreeDays = interestFreeDays;
        this.maxCreditLimit = AssertUtil.requireNotNull(maxCreditLimit, "Max credit limit cannot be null");
        this.minCreditLimit = AssertUtil.requireNotNull(minCreditLimit, "Min credit limit cannot be null");
        if (this.minCreditLimit.compareTo(this.maxCreditLimit) > 0) {
            throw new IllegalArgumentException("Min credit limit cannot be greater than max credit limit");
        }
    }

    public UUID getProgramId() {
        return programId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public String getAirLoyaltyPlan() {
        return airLoyaltyPlan;
    }

    public Integer getInterestFreeDays() {
        return interestFreeDays;
    }

    public BigInteger getMaxCreditLimit() {
        return maxCreditLimit;
    }

    public BigInteger getMinCreditLimit() {
        return minCreditLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CardProgram other)) {
            return false;
        }
        return programId != null && programId.equals(other.programId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CardProgram{programId=%s, name=%s}".formatted(programId, name);
    }
}
