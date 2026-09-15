USE cards;
CREATE TABLE transactions (
    transaction_id CHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    trans_date DATETIME NOT NULL,
    merchant_name VARCHAR(150) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transactions_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);
