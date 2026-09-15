package com.example.cardsdbccp.model;

import com.example.cardsdbccp.util.AssertUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "customer")
public class Customer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", updatable = false, nullable = false)
    private UUID customerId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mobile", nullable = false)
    private Long mobile;

    @Column(name = "dob")
    private LocalDate dob;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CustomerCardSummary cardSummary;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Transaction> transactions = new ArrayList<>();

    protected Customer() {
        // required by JPA
    }

    public Customer(String firstName, String lastName, String email, Long mobile, LocalDate dob) {
        this.firstName = AssertUtil.requireNotBlank(firstName, "First name cannot be null or empty");
        this.lastName = AssertUtil.requireNotBlank(lastName, "Last name cannot be null or empty");
        this.email = validateEmail(email);
        this.mobile = AssertUtil.requireNotNull(mobile, "Mobile cannot be null");
        this.dob = dob;
    }

    private static String validateEmail(String email) {
        AssertUtil.requireNotBlank(email, "Email cannot be null or empty");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email;
    }

    public void assignCardSummary(CustomerCardSummary cardSummary) {
        this.cardSummary = cardSummary;
        if (cardSummary != null) {
            cardSummary.setCustomer(this);
        }
    }

    public void addTransaction(Transaction transaction) {
        AssertUtil.requireNotNull(transaction, "Transaction cannot be null");
        transactions.add(transaction);
        transaction.setCustomer(this);
    }

    public void removeTransaction(Transaction transaction) {
        AssertUtil.requireNotNull(transaction, "Transaction cannot be null");
        transactions.remove(transaction);
        transaction.setCustomer(null);
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = AssertUtil.requireNotBlank(firstName, "First name cannot be null or empty");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = AssertUtil.requireNotBlank(lastName, "Last name cannot be null or empty");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    public Long getMobile() {
        return mobile;
    }

    public void setMobile(Long mobile) {
        this.mobile = AssertUtil.requireNotNull(mobile, "Mobile cannot be null");
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public CustomerCardSummary getCardSummary() {
        return cardSummary;
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer other)) {
            return false;
        }
        return customerId != null && customerId.equals(other.customerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Customer{customerId=%s, firstName=%s, lastName=%s, email=%s}"
                .formatted(customerId, firstName, lastName, email);
    }
}
