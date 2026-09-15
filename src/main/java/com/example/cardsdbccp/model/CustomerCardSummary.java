package com.example.cardsdbccp.model;

import com.example.cardsdbccp.util.AssertUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customer_card_summary")
public class CustomerCardSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "summary_id", updatable = false, nullable = false)
    private UUID summaryId;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private CardProgram cardProgram;

    @Column(name = "programme_name", nullable = false)
    private String programmeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    private CardStatus cardStatus;

    @Column(name = "fraud_flag", nullable = false)
    private Boolean fraudFlag;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "credit_outstanding", nullable = false)
    private BigDecimal creditOutstanding;

    protected CustomerCardSummary() {
        // required by JPA
    }

    public CustomerCardSummary(
            CardProgram cardProgram,
            String programmeName,
            CardStatus cardStatus,
            Boolean fraudFlag,
            LocalDate expiryDate,
            BigDecimal creditOutstanding) {
        this.cardProgram = AssertUtil.requireNotNull(cardProgram, "Card program cannot be null");
        this.programmeName = AssertUtil.requireNotBlank(programmeName, "Programme name cannot be null or empty");
        this.cardStatus = AssertUtil.requireNotNull(cardStatus, "Card status cannot be null");
        this.fraudFlag = AssertUtil.requireNotNull(fraudFlag, "Fraud flag cannot be null");
        this.expiryDate = AssertUtil.requireNotNull(expiryDate, "Expiry date cannot be null");
        this.creditOutstanding = AssertUtil.requireNotNull(creditOutstanding, "Credit outstanding cannot be null");
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public UUID getSummaryId() {
        return summaryId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public CardProgram getCardProgram() {
        return cardProgram;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = AssertUtil.requireNotNull(cardStatus, "Card status cannot be null");
    }

    public Boolean getFraudFlag() {
        return fraudFlag;
    }

    public void setFraudFlag(Boolean fraudFlag) {
        this.fraudFlag = AssertUtil.requireNotNull(fraudFlag, "Fraud flag cannot be null");
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public BigDecimal getCreditOutstanding() {
        return creditOutstanding;
    }

    public void setCreditOutstanding(BigDecimal creditOutstanding) {
        this.creditOutstanding = AssertUtil.requireNotNull(creditOutstanding, "Credit outstanding cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerCardSummary other)) {
            return false;
        }
        return summaryId != null && summaryId.equals(other.summaryId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CustomerCardSummary{summaryId=%s, cardStatus=%s}".formatted(summaryId, cardStatus);
    }
}
