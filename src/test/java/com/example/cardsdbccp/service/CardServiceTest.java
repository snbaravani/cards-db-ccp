package com.example.cardsdbccp.service;

import com.example.cardsdbccp.dto.CardProgramResponse;
import com.example.cardsdbccp.dto.CustomerCardSummaryResponse;
import com.example.cardsdbccp.dto.CustomerResponse;
import com.example.cardsdbccp.model.CardProgram;
import com.example.cardsdbccp.model.CardStatus;
import com.example.cardsdbccp.model.Customer;
import com.example.cardsdbccp.model.CustomerCardSummary;
import com.example.cardsdbccp.repository.CardProgramRepository;
import com.example.cardsdbccp.repository.CustomerCardSummaryRepository;
import com.example.cardsdbccp.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardProgramRepository cardProgramRepository;

    @Mock
    private CustomerCardSummaryRepository customerCardSummaryRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(customerRepository, cardProgramRepository, customerCardSummaryRepository);
    }

    private static Customer buildCustomer(UUID id, String firstName, String lastName, String email, Long mobile) {
        Customer customer = new Customer(firstName, lastName, email, mobile, LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(customer, "customerId", id);
        return customer;
    }

    private static CardProgram buildCardProgram(UUID id, String name) {
        CardProgram cardProgram = new CardProgram(
                name, new BigDecimal("19.99"), "SkyMiles", 45, BigInteger.valueOf(50000), BigInteger.valueOf(5000));
        ReflectionTestUtils.setField(cardProgram, "programId", id);
        return cardProgram;
    }

    // ---- searchCustomers ----

    @Test
    void searchCustomers_byLastNameAndEmail_returnsMatches(McpSyncRequestContext context) {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        when(customerRepository.findByLastNameIgnoreCaseAndEmailIgnoreCase("Doe", "john.doe@example.com"))
                .thenReturn((customer));

        CustomerResponse result = cardService.searchCustomer( context,"john.doe@example.com");

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.email()).isEqualTo("john.doe@example.com");
        verify(customerRepository, never()).findByLastNameIgnoreCaseAndMobile(any(), any());
    }

//    @Test
//    void searchCustomers_byLastNameAndMobile_returnsMatches() {
//        UUID customerId = UUID.randomUUID();
//        Customer customer = buildCustomer(customerId, "Jane", "Smith", "jane.smith@example.com", 4155555678L);
//        when(customerRepository.findByLastNameIgnoreCaseAndMobile("Smith", 4155555678L))
//                .thenReturn(customer);
//
//        CustomerResponse result = cardService.searchCustomers("Smith", null, 4155555678L);
//
//        assertThat(result.lastName()).isEqualTo("Smith");
//    }

//    @Test
//    void searchCustomers_byEmailOnly_returnsMatches() {
//        UUID customerId = UUID.randomUUID();
//        Customer customer = buildCustomer(customerId, "Alice", "Brown", "alice.brown@example.com", 4155559012L);
//        when(customerRepository.findByEmailIgnoreCase("alice.brown@example.com")).thenReturn(customer);
//
//        CustomerResponse result = cardService.searchCustomers(null, "alice.brown@example.com", null);
//
//        assertThat(result.customerId()).isEqualTo(customerId);
//    }

//    @Test
//    void searchCustomers_byMobileOnly_returnsMatches() {
//        UUID customerId = UUID.randomUUID();
//        Customer customer = buildCustomer(customerId, "Alice", "Brown", "alice.brown@example.com", 4155559012L);
//        when(customerRepository.findByMobile(4155559012L)).thenReturn(customer);
//
//        CustomerResponse result = cardService.searchCustomers(null, null, 4155559012L);
//
//        assertThat(result.mobile()).isEqualTo(4155559012L);
//    }

//    @Test
//    void searchCustomers_lastNameWithoutEmailOrMobile_throwsIllegalArgumentException() {
//        assertThatThrownBy(() -> cardService.searchCustomers("Doe", null, null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Last name must be accompanied");
//    }

//    @Test
//    void searchCustomers_noCriteria_throwsIllegalArgumentException() {
//        assertThatThrownBy(() -> cardService.searchCustomers(null, null, null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("At least an e-mail or a mobile number");
//    }
//
//    // ---- getCardProgramsByName ----

    @Test
    void getCardProgramsByName_returnsMatches(McpSyncRequestContext context) {
        UUID programId = UUID.randomUUID();
        CardProgram cardProgram = buildCardProgram(programId, "Platinum Rewards");
        when(cardProgramRepository.findByNameIgnoreCase("Platinum Rewards")).thenReturn(List.of(cardProgram));

        List<CardProgramResponse> result = cardService.getCardProgramsByName(context, "Platinum Rewards");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().programId()).isEqualTo(programId);
        assertThat(result.getFirst().name()).isEqualTo("Platinum Rewards");
    }

    @Test
    void getCardProgramsByName_blankName_throwsIllegalArgumentException(McpSyncRequestContext context) {
        assertThatThrownBy(() -> cardService.getCardProgramsByName(context," "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cardProgramRepository, never()).findByNameIgnoreCase(any());
    }

    // ---- getCardSummaryByCustomerId ----

    @Test
    void getCardSummaryByCustomerId_found_returnsSummary(McpSyncRequestContext context) {
        UUID customerId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        CardProgram cardProgram = buildCardProgram(programId, "Platinum Rewards");
        CustomerCardSummary summary = new CustomerCardSummary(
                cardProgram, "Platinum Rewards", CardStatus.ACTIVE, false, LocalDate.of(2028, 6, 30),
                new BigDecimal("1250.75"));
        customer.assignCardSummary(summary);

        when(customerCardSummaryRepository.findByCustomer_CustomerId(customerId)).thenReturn(Optional.of(summary));

        CustomerCardSummaryResponse result = cardService.getCardSummaryByCustomerEmail(context, customer.getEmail());

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.programId()).isEqualTo(programId);
        assertThat(result.cardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(result.creditOutstanding()).isEqualByComparingTo("1250.75");
    }



    @Test
    void getCardSummaryByCustomerId_nullId_throwsIllegalArgumentException(McpSyncRequestContext context) {
        assertThatThrownBy(() -> cardService.getCardSummaryByCustomerEmail(context,null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(customerCardSummaryRepository, never()).findByCustomer_CustomerId(any());
    }
}
