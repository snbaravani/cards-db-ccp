package com.example.cardsdbccp.service;

import com.example.cardsdbccp.dto.CardProgramResponse;
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
import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardService {

    private final CustomerRepository customerRepository;
    private final CardProgramRepository cardProgramRepository;
    private final CustomerCardSummaryRepository customerCardSummaryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CardService.class);




    public CardService(
            CustomerRepository customerRepository,
            CardProgramRepository cardProgramRepository,
            CustomerCardSummaryRepository customerCardSummaryRepository) {
        this.customerRepository = customerRepository;
        this.cardProgramRepository = cardProgramRepository;
        this.customerCardSummaryRepository = customerCardSummaryRepository;
    }

    /**
     * Searches customers by last name, e-mail and/or mobile.
     * If a last name is supplied, either the e-mail or the mobile must also be supplied.
     * Otherwise, the e-mail or the mobile alone is sufficient to search.
     */
    @McpTool(name = "search_customer",
            description = "Search a customer based on the email and return customer details")
    public CustomerResponse searchCustomer(McpSyncRequestContext context, @ToolParam(description = "customer email", required = true) String email) {
        logger.info("searchCustomer->");
        Customer customers;
        if (email != null) {
            customers = customerRepository.findByEmailIgnoreCase(email);
        } else {
            throw new IllegalArgumentException("E-mail must be provided");
        }
        context.info( "MCP Server: searchCustomer -> " );
        return toCustomerResponse(customers);
    }

    /** Fetches card programs matching the given name. */
    @McpTool(name = "search_card_program_by_name",
            description = "Fetch the card program details based on the card program name.")
    public List<CardProgramResponse> getCardProgramsByName(McpSyncRequestContext context, @ToolParam(description = "Card name", required = true) String name) {
        logger.info("getCardProgramsByName->");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Card program name must be provided");
        }
        context.info( "MCP Server: getCardProgramsByName -> " );
        return cardProgramRepository.findByNameIgnoreCase(name).stream()
                .map(CardService::toCardProgramResponse)
                .toList();
    }

    /** Fetches the card summary for a given customer id. */
    @McpTool(name = "search_card_summary_by_customer_email",
            description = "Fetch the card  summary details based on the customer email.")
    public CustomerCardSummaryResponse getCardSummaryByCustomerEmail(McpSyncRequestContext context, @ToolParam(description = "customer email", required = true) String email) {
        context.info( "MCP Server: getCardSummaryByCustomerEmail ==> " +email);
        logger.info("getCardSummaryByCustomerEmail->");
        if (email == null) {
            throw new IllegalArgumentException("email id must be provided");
        }
        System.out.println("getCardSummaryByCustomerEmail->");
        CustomerCardSummary summary = customerCardSummaryRepository
                .findByCustomer_Email(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No card summary found for customer email id: " + email));

        return toCustomerCardSummaryResponse(summary);
    }
    @McpTool(name = "close_customer_card",
            description = "Close the customer card if there is no outstanding and the card is not flagged")
    public String closeCard(McpSyncRequestContext context, @ToolParam(description = "customer email", required = true)  String email){
        logger.info("closeCard-> {}", email);
        context.info( "MCP Server: closeCard ->"+ email );
        Customer customer =  customerRepository.findByEmailIgnoreCase(email) ;
        CustomerCardSummary summary = customerCardSummaryRepository.findByCustomer_CustomerId(customer.getCustomerId()).get();
        if(summary.getFraudFlag() != true && summary.getCreditOutstanding().doubleValue() <= 0){

            summary.setCardStatus(CardStatus.CLOSED);
            context.info( "Closing the card for  ->"+ email +" >"+summary.getCardStatus() );
            customerCardSummaryRepository.save(summary);
            return "Successfully closed the customer card";
        } else{
             return "Can't close the card. Please call the customer care on 010-32311";
        }

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
