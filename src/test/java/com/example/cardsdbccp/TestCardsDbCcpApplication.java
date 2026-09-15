package com.example.cardsdbccp;

import org.springframework.boot.SpringApplication;

public class TestCardsDbCcpApplication {

    public static void main(String[] args) {
        SpringApplication.from(CardsDbCcpApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
