package com.example.cardsdbccp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cardsdbccp.dto.CardProgramResponse;
import com.example.cardsdbccp.dto.CloseCardResponse;
import com.example.cardsdbccp.dto.CustomerCardSummaryResponse;
import com.example.cardsdbccp.dto.CustomerResponse;
import com.example.cardsdbccp.exception.ResourceNotFoundException;
import com.example.cardsdbccp.model.CardProgram;
import com.example.cardsdbccp.model.CardStatus;
import com.example.cardsdbccp.model.Customer;
import com.example.cardsdbccp.model.CustomerCardSummary;
import com.example.cardsdbccp.repository.CardProgramRepository;
import com.example.cardsdbccp.repository.CustomerCardSummaryRepository;
import com.example.cardsdbccp.repository.CustomerRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    private static CustomerCardSummary buildSummary(
            CardProgram cardProgram, boolean fraudFlag, BigDecimal creditOutstanding) {
        return new CustomerCardSummary(
                cardProgram,
                "Platinum Rewards",
                CardStatus.ACTIVE,
                fraudFlag,
                LocalDate.of(2028, 6, 30),
                creditOutstanding);
    }

    // ---- searchCustomer ----

    @Test
    void searchCustomer_found_returnsCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        when(customerRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(customer);

        CustomerResponse result = cardService.searchCustomer("john.doe@example.com");

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.email()).isEqualTo("john.doe@example.com");
    }

    @Test
    void searchCustomer_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(null);

        assertThatThrownBy(() -> cardService.searchCustomer("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchCustomer_blankEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cardService.searchCustomer(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(customerRepository, never()).findByEmailIgnoreCase(any());
    }

    // ---- getCardProgramsByName ----

    @Test
    void getCardProgramsByName_returnsMatches() {
        UUID programId = UUID.randomUUID();
        CardProgram cardProgram = buildCardProgram(programId, "Platinum Rewards");
        when(cardProgramRepository.findByNameIgnoreCase("Platinum Rewards")).thenReturn(List.of(cardProgram));

        List<CardProgramResponse> result = cardService.getCardProgramsByName("Platinum Rewards");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().programId()).isEqualTo(programId);
        assertThat(result.getFirst().name()).isEqualTo("Platinum Rewards");
    }

    @Test
    void getCardProgramsByName_blankName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cardService.getCardProgramsByName(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cardProgramRepository, never()).findByNameIgnoreCase(any());
    }

    // ---- getCardSummaryByCustomerEmail ----

    @Test
    void getCardSummaryByCustomerEmail_found_returnsSummary() {
        UUID customerId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        CardProgram cardProgram = buildCardProgram(programId, "Platinum Rewards");
        CustomerCardSummary summary = buildSummary(cardProgram, false, new BigDecimal("1250.75"));
        customer.assignCardSummary(summary);

        when(customerCardSummaryRepository.findByCustomer_Email("john.doe@example.com"))
                .thenReturn(Optional.of(summary));

        CustomerCardSummaryResponse result = cardService.getCardSummaryByCustomerEmail("john.doe@example.com");

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.programId()).isEqualTo(programId);
        assertThat(result.cardStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(result.creditOutstanding()).isEqualByComparingTo("1250.75");
    }

    @Test
    void getCardSummaryByCustomerEmail_notFound_throwsResourceNotFoundException() {
        when(customerCardSummaryRepository.findByCustomer_Email("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardSummaryByCustomerEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCardSummaryByCustomerEmail_blankEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cardService.getCardSummaryByCustomerEmail(null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(customerCardSummaryRepository, never()).findByCustomer_Email(any());
    }

    // ---- closeCard ----

    @Test
    void closeCard_eligible_closesCardAndReturnsSuccessResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        CardProgram cardProgram = buildCardProgram(UUID.randomUUID(), "Platinum Rewards");
        CustomerCardSummary summary = buildSummary(cardProgram, false, BigDecimal.ZERO);

        when(customerRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(customer);
        when(customerCardSummaryRepository.findByCustomer_CustomerId(customerId)).thenReturn(Optional.of(summary));

        CloseCardResponse result = cardService.closeCard("john.doe@example.com");

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.isError()).isFalse();
        assertThat(summary.getCardStatus()).isEqualTo(CardStatus.CLOSED);
        verify(customerCardSummaryRepository).save(summary);
    }

    @Test
    void closeCard_fraudFlagged_doesNotCloseAndReturnsErrorResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        CardProgram cardProgram = buildCardProgram(UUID.randomUUID(), "Platinum Rewards");
        CustomerCardSummary summary = buildSummary(cardProgram, true, BigDecimal.ZERO);

        when(customerRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(customer);
        when(customerCardSummaryRepository.findByCustomer_CustomerId(customerId)).thenReturn(Optional.of(summary));

        CloseCardResponse result = cardService.closeCard("john.doe@example.com");

        assertThat(result.isError()).isTrue();
        assertThat(summary.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(customerCardSummaryRepository, never()).save(any());
    }

    @Test
    void closeCard_outstandingBalance_doesNotCloseAndReturnsErrorResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId, "John", "Doe", "john.doe@example.com", 4155551234L);
        CardProgram cardProgram = buildCardProgram(UUID.randomUUID(), "Platinum Rewards");
        CustomerCardSummary summary = buildSummary(cardProgram, false, new BigDecimal("100.00"));

        when(customerRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(customer);
        when(customerCardSummaryRepository.findByCustomer_CustomerId(customerId)).thenReturn(Optional.of(summary));

        CloseCardResponse result = cardService.closeCard("john.doe@example.com");

        assertThat(result.isError()).isTrue();
        assertThat(summary.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(customerCardSummaryRepository, never()).save(any());
    }

    @Test
    void closeCard_customerNotFound_throwsResourceNotFoundException() {
        when(customerRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(null);

        assertThatThrownBy(() -> cardService.closeCard("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(customerCardSummaryRepository, never()).findByCustomer_CustomerId(any());
    }

    @Test
    void closeCard_blankEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> cardService.closeCard(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(customerRepository, never()).findByEmailIgnoreCase(any());
    }
}
