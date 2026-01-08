package com.obp.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obp.demo.config.ObpApiConfig;
import com.obp.demo.model.Account;
import com.obp.demo.model.Counterparty;
import com.obp.demo.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObpApiService {

    private final WebClient webClient;
    private final ObpApiConfig obpApiConfig;
    private final ObjectMapper objectMapper;

    private String getApiUrl(String endpoint) {
        return obpApiConfig.getBaseUrl() + "/obp/" + obpApiConfig.getVersion() + endpoint;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "DirectLogin token=\"" + obpApiConfig.getApiKey() + "\"");
        return headers;
    }

    private HttpHeaders getHeadersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "DirectLogin token=\"" + token + "\"");
        return headers;
    }

    public String createUser(String username, String email, String password, String firstName, String lastName) {
        try {
            Map<String, Object> userData = Map.of(
                "username", username,
                "email", email,
                "password", password,
                "first_name", firstName,
                "last_name", lastName
            );

            String response = webClient.post()
                    .uri(getApiUrl("/users"))
                    .headers(h -> h.addAll(getHeaders()))
                    .bodyValue(userData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("User created: {}", response);
            
            // Extract user_id from response
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("user_id").asText();
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public String authenticateUser(String username, String password) {
        try {
            Map<String, String> loginData = Map.of(
                "username", username,
                "password", password,
                "consumer_key", obpApiConfig.getApiKey()
            );

            String response = webClient.post()
                    .uri(getApiUrl("/my/logins/direct"))
                    .headers(h -> h.addAll(getHeaders()))
                    .bodyValue(loginData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Authentication response: {}", response);
            
            // Extract token from response
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("token").asText();
        } catch (Exception e) {
            log.error("Error authenticating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate user: " + e.getMessage(), e);
        }
    }

    public List<Account> getAccounts(String token) {
        try {
            // First, get banks
            String banksResponse = webClient.get()
                    .uri(getApiUrl("/banks"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode banksNode = objectMapper.readTree(banksResponse);
            if (!banksNode.has("banks") || banksNode.get("banks").isEmpty()) {
                return Collections.emptyList();
            }

            String bankId = banksNode.get("banks").get(0).path("id").asText();
            
            // Get accounts for the first bank
            String accountsResponse = webClient.get()
                    .uri(getApiUrl("/banks/" + bankId + "/accounts"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode accountsNode = objectMapper.readTree(accountsResponse);
            if (!accountsNode.has("accounts")) {
                return Collections.emptyList();
            }

            List<Account> accounts = objectMapper.readValue(
                    accountsNode.get("accounts").toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Account.class)
            );

            // Enrich accounts with balance
            for (Account account : accounts) {
                try {
                    String balanceResponse = webClient.get()
                            .uri(getApiUrl("/banks/" + bankId + "/accounts/" + account.getId() + "/account/balance"))
                            .headers(h -> h.addAll(getHeadersWithToken(token)))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode balanceNode = objectMapper.readTree(balanceResponse);
                    if (balanceNode.has("balance")) {
                        Account.Balance balance = new Account.Balance();
                        balance.setCurrency(balanceNode.path("balance").path("currency").asText());
                        balance.setAmount(balanceNode.path("balance").path("amount").asText());
                        account.setBalance(balance);
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch balance for account {}: {}", account.getId(), e.getMessage());
                }
            }

            return accounts;
        } catch (Exception e) {
            log.error("Error fetching accounts: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Transaction> getTransactions(String token, String bankId, String accountId) {
        try {
            String response = webClient.get()
                    .uri(getApiUrl("/banks/" + bankId + "/accounts/" + accountId + "/transactions"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (!jsonNode.has("transactions")) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(
                    jsonNode.get("transactions").toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class)
            );
        } catch (Exception e) {
            log.error("Error fetching transactions: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Counterparty> getCounterparties(String token, String bankId, String accountId) {
        try {
            String response = webClient.get()
                    .uri(getApiUrl("/banks/" + bankId + "/accounts/" + accountId + "/counterparties"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (!jsonNode.has("counterparties")) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(
                    jsonNode.get("counterparties").toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Counterparty.class)
            );
        } catch (Exception e) {
            log.error("Error fetching counterparties: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public String getBankId(String token) {
        try {
            String response = webClient.get()
                    .uri(getApiUrl("/banks"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("banks") && jsonNode.get("banks").size() > 0) {
                return jsonNode.get("banks").get(0).path("id").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching bank ID: {}", e.getMessage(), e);
            return null;
        }
    }
}
