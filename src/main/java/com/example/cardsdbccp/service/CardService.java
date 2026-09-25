package com.example.cardsdbccp.service;

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
import com.example.cardsdbccp.util.AssertUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CustomerRepository customerRepository;
    private final CardProgramRepository cardProgramRepository;
    private final CustomerCardSummaryRepository customerCardSummaryRepository;

    /** Looks up a customer by e-mail. */
    public CustomerResponse searchCustomer(String email) {
        AssertUtil.requireNotBlank(email, "E-mail must be provided");
        log.info("searchCustomer -> {}", email);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return toCustomerResponse(findCustomerByEmail(email));
    }

    /** Fetches card programs matching the given name. */
    public List<CardProgramResponse> getCardProgramsByName(String name) {
        AssertUtil.requireNotBlank(name, "Card program name must be provided");
        log.info("getCardProgramsByName -> {}", name);
        return cardProgramRepository.findByNameIgnoreCase(name).stream()
                .map(CardService::toCardProgramResponse)
                .toList();
    }

    /** Fetches the card summary for a given customer e-mail. */
    public CustomerCardSummaryResponse getCardSummaryByCustomerEmail(String email) {
        AssertUtil.requireNotBlank(email, "E-mail must be provided");
        log.info("getCardSummaryByCustomerEmail -> {}", email);
        CustomerCardSummary summary = customerCardSummaryRepository
                .findByCustomer_Email(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No card summary found for customer email id: " + email));
        return toCustomerCardSummaryResponse(summary);
    }

    /** Closes the customer's card if there is no outstanding balance and it is not flagged for fraud. */
    @Transactional
    public CloseCardResponse closeCard(String email) {
        AssertUtil.requireNotBlank(email, "E-mail must be provided");
        log.info("closeCard -> {}", email);
        Customer customer = findCustomerByEmail(email);
        CustomerCardSummary summary = customerCardSummaryRepository
                .findByCustomer_CustomerId(customer.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("No card summary found for customer email id: " + email));

        if (!summary.getFraudFlag() && summary.getCreditOutstanding().doubleValue() <= 0) {
            summary.setCardStatus(CardStatus.CLOSED);
            customerCardSummaryRepository.save(summary);
            log.info("Closed card for {} -> {}", email, summary.getCardStatus());
            return new CloseCardResponse(
                    customer.getCustomerId(), "Successfully closed the customer card", false, false);
        }
        return new CloseCardResponse(
                customer.getCustomerId(), "Can't close the card. Please call the customer care on 010-32311", true, false);
    }

    private Customer findCustomerByEmail(String email) {
        return Optional.ofNullable(customerRepository.findByEmailIgnoreCase(email))
                .orElseThrow(() -> new ResourceNotFoundException("No customer found for email: " + email));
    }

    private static CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getMobile(),
                customer.getDob());
    }

    private static CardProgramResponse toCardProgramResponse(CardProgram cardProgram) {
        return new CardProgramResponse(
                cardProgram.getProgramId(),
                cardProgram.getName(),
                cardProgram.getInterestRate(),
                cardProgram.getAirLoyaltyPlan(),
                cardProgram.getInterestFreeDays(),
                cardProgram.getMaxCreditLimit(),
                cardProgram.getMinCreditLimit());
    }

    private static CustomerCardSummaryResponse toCustomerCardSummaryResponse(CustomerCardSummary summary) {
        return new CustomerCardSummaryResponse(
                summary.getCustomer().getCustomerId(),
                summary.getCardProgram().getProgramId(),
                summary.getProgrammeName(),
                summary.getCardStatus(),
                summary.getFraudFlag(),
                summary.getExpiryDate(),
                summary.getCreditOutstanding());
    }
}
