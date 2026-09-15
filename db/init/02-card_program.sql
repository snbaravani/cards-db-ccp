USE cards;
CREATE TABLE card_program (
    program_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    air_loyalty_plan VARCHAR(150),
    interest_free_days INT,
    max_credit_limit DECIMAL(30,0) NOT NULL,
    min_credit_limit DECIMAL(30,0) NOT NULL,
    PRIMARY KEY (program_id)
);
