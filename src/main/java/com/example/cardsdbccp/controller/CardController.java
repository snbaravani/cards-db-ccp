package com.example.cardsdbccp.controller;

import com.example.cardsdbccp.dto.CardProgramResponse;
import com.example.cardsdbccp.dto.CloseCardResponse;
import com.example.cardsdbccp.dto.CustomerCardSummaryResponse;
import com.example.cardsdbccp.dto.CustomerResponse;
import com.example.cardsdbccp.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Card Support", description = "Look up customers, card programs and card summaries, and close cards")
public class CardController {

    private final CardService cardService;

    @GetMapping("/customers/{email}")
    @Operation(summary = "Search a customer by email", description = "Returns customer details for the given email")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "No customer found for the given email")
    public CustomerResponse searchCustomer(
            @Parameter(description = "Customer email", required = true) @PathVariable String email) {
        return cardService.searchCustomer(email);
    }

    @GetMapping("/card-programs")
    @Operation(
            summary = "Search card programs by name",
            description = "Returns the card program details matching the given name")
    @ApiResponse(responseCode = "200", description = "Card programs found")
    public List<CardProgramResponse> getCardProgramsByName(
            @Parameter(description = "Card program name", required = true) @RequestParam String name) {
        return cardService.getCardProgramsByName(name);
    }

    @GetMapping("/customers/{email}/card-summary")
    @Operation(
            summary = "Get a customer's card summary",
            description = "Returns the card summary for the given customer email")
    @ApiResponse(responseCode = "200", description = "Card summary found")
    @ApiResponse(responseCode = "404", description = "No card summary found for the given email")
    public CustomerCardSummaryResponse getCardSummaryByCustomerEmail(
            @Parameter(description = "Customer email", required = true) @PathVariable String email) {
        return cardService.getCardSummaryByCustomerEmail(email);
    }

    @PostMapping("/customers/{email}/close-card")
    @Operation(
            summary = "Close a customer's card",
            description = "Closes the card if there is no outstanding balance and it is not flagged for fraud")
    @ApiResponse(responseCode = "200", description = "Card close request processed")
    @ApiResponse(responseCode = "404", description = "No customer or card summary found for the given email")
    public CloseCardResponse closeCard(
            @Parameter(description = "Customer email", required = true) @PathVariable String email) {
        return cardService.closeCard(email);
    }

    @PostMapping("/customers/{email}/close-cardtst")
    @Operation(
            summary = "Close a customer's cardasdaa",
            description = "Closes the card if there is no outstanding balance and it is not flagged for fraud")
    @ApiResponse(responseCode = "200", description = "Card close request processed")
    @ApiResponse(responseCode = "404", description1 = "No customer or card summary found for the given email")
    public CloseCardResponse closeCardTest(
            @Parameter(description = "Customeraaa email", required = true) @PathVariable String email) {

        int X = 0+1;
        return null;
    }
}
