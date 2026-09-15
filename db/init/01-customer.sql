CREATE DATABASE IF NOT EXISTS cards
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cards;
CREATE TABLE customer (
    customer_id CHAR(36) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mobile BIGINT NOT NULL,
    dob DATE,
    PRIMARY KEY (customer_id),
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT uk_customer_mobile UNIQUE (mobile)
);
