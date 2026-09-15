package com.example.cardsdbccp.model;

import com.example.cardsdbccp.util.AssertUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "trans_date", nullable = false)
    private LocalDateTime transDate;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    protected Transaction() {
        // required by JPA
    }

    public Transaction(
            BigDecimal amount, LocalDateTime transDate, String merchantName, String city, String country) {
        this.amount = AssertUtil.requireNotNull(amount, "Amount cannot be null");
        this.transDate = AssertUtil.requireNotNull(transDate, "Transaction date cannot be null");
        this.merchantName = AssertUtil.requireNotBlank(merchantName, "Merchant name cannot be null or empty");
        this.city = city;
        this.country = country;
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTransDate() {
        return transDate;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction other)) {
            return false;
        }
        return transactionId != null && transactionId.equals(other.transactionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{transactionId=%s, amount=%s, merchantName=%s}"
                .formatted(transactionId, amount, merchantName);
    }
}
