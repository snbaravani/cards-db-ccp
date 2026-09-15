USE cards;
CREATE TABLE customer_card_summary (
    summary_id CHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    program_id CHAR(36) NOT NULL,
    programme_name VARCHAR(150) NOT NULL,
    card_status VARCHAR(20) NOT NULL,
    fraud_flag BOOLEAN NOT NULL,
    expiry_date DATE NOT NULL,
    credit_outstanding DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (summary_id),
    CONSTRAINT uk_customer_card_summary_customer UNIQUE (customer_id),
    CONSTRAINT fk_customer_card_summary_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_customer_card_summary_program FOREIGN KEY (program_id) REFERENCES card_program (program_id),
    CONSTRAINT chk_customer_card_summary_status CHECK (card_status IN ('ACTIVE', 'BLOCKED', 'INACTIVE', 'EXPIRED'))
);
