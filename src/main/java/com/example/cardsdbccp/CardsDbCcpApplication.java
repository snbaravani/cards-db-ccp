package com.example.cardsdbccp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Card Customer Support API",
                        version = "0.0.1",
                        description = "REST API for a credit-card customer support agent: look up customers, "
                                + "card programs, card summaries, and close cards."))
public class CardsDbCcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardsDbCcpApplication.class, args);
    }

}
