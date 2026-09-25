package com.example.cardsdbccp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.cardsdbccp.dto.CardProgramResponse;
import com.example.cardsdbccp.dto.CloseCardResponse;
import com.example.cardsdbccp.dto.CustomerCardSummaryResponse;
import com.example.cardsdbccp.dto.CustomerResponse;
import com.example.cardsdbccp.exception.ResourceNotFoundException;
import com.example.cardsdbccp.model.CardStatus;
import com.example.cardsdbccp.service.CardService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CardService cardService;

    @Test
    void searchCustomer_found_returnsCustomerJson() {
        UUID customerId = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                customerId, "John", "Doe", "john.doe@example.com", 4155551234L, LocalDate.of(1990, 1, 1));
        when(cardService.searchCustomer("john.doe@example.com")).thenReturn(response);

        assertThat(mockMvc.get().uri("/api/customers/john.doe@example.com"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.email")
                .isEqualTo("john.doe@example.com");
    }

    @Test
    void searchCustomer_notFound_returns404() {
        when(cardService.searchCustomer(anyString()))
                .thenThrow(new ResourceNotFoundException("No customer found for email: missing@example.com"));

        assertThat(mockMvc.get().uri("/api/customers/missing@example.com")).hasStatus(404);
    }

    @Test
    void getCardProgramsByName_returnsMatches() {
        CardProgramResponse response = new CardProgramResponse(
                UUID.randomUUID(),
                "Platinum Rewards",
                new BigDecimal("19.99"),
                "SkyMiles",
                45,
                BigInteger.valueOf(50000),
                BigInteger.valueOf(5000));
        when(cardService.getCardProgramsByName("Platinum Rewards")).thenReturn(List.of(response));

        assertThat(mockMvc.get().uri("/api/card-programs").param("name", "Platinum Rewards"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Platinum Rewards");
    }

    @Test
    void getCardSummary_found_returnsSummaryJson() {
        CustomerCardSummaryResponse response = new CustomerCardSummaryResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Platinum Rewards",
                CardStatus.ACTIVE,
                false,
                LocalDate.of(2028, 6, 30),
                new BigDecimal("1250.75"));
        when(cardService.getCardSummaryByCustomerEmail("john.doe@example.com")).thenReturn(response);

        assertThat(mockMvc.get().uri("/api/customers/john.doe@example.com/card-summary"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.cardStatus")
                .isEqualTo("ACTIVE");
    }

    @Test
    void getCardSummary_notFound_returns404() {
        when(cardService.getCardSummaryByCustomerEmail(anyString()))
                .thenThrow(new ResourceNotFoundException("No card summary found for customer email id: missing@example.com"));

        assertThat(mockMvc.get().uri("/api/customers/missing@example.com/card-summary")).hasStatus(404);
    }

    @Test
    void closeCard_eligible_returnsSuccessJson() {
        UUID customerId = UUID.randomUUID();
        CloseCardResponse response =
                new CloseCardResponse(customerId, "Successfully closed the customer card", false, false);
        when(cardService.closeCard("john.doe@example.com")).thenReturn(response);

        assertThat(mockMvc.post().uri("/api/customers/john.doe@example.com/close-card"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.response")
                .isEqualTo("Successfully closed the customer card");
    }
}
