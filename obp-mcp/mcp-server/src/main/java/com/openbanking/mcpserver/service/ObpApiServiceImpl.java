package com.openbanking.mcpserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openbanking.mcpserver.config.ObpApiConfig;
import com.openbanking.mcpserver.exception.ObpApiException;
import com.openbanking.mcpserver.model.Account;
import com.openbanking.mcpserver.model.Balance;
import com.openbanking.mcpserver.model.Counterparty;
import com.openbanking.mcpserver.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObpApiServiceImpl implements ObpApiService {

    private final WebClient webClient;
    private final ObpApiConfig obpApiConfig;
    private final ObjectMapper objectMapper;

    private String getApiUrl(String endpoint) {
        return obpApiConfig.getBaseUrl() + "/obp/" + obpApiConfig.getVersion() + endpoint;
    }

    private HttpHeaders getHeadersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "DirectLogin token=\"" + token + "\"");
        return headers;
    }

    @Override
    public List<Account> getAccounts(String token) {
        try {
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

            return objectMapper.readValue(
                    accountsNode.get("accounts").toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Account.class)
            );
        } catch (Exception e) {
            log.error("Error fetching accounts: {}", e.getMessage(), e);
            throw new ObpApiException("Failed to fetch accounts: " + e.getMessage(), e);
        }
    }

    @Override
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
            throw new ObpApiException("Failed to fetch transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public Balance getAccountBalance(String token, String bankId, String accountId) {
        try {
            String response = webClient.get()
                    .uri(getApiUrl("/banks/" + bankId + "/accounts/" + accountId + "/account/balance"))
                    .headers(h -> h.addAll(getHeadersWithToken(token)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("balance")) {
                Balance balance = new Balance();
                balance.setCurrency(jsonNode.path("balance").path("currency").asText());
                balance.setAmount(jsonNode.path("balance").path("amount").asText());
                return balance;
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching account balance: {}", e.getMessage(), e);
            throw new ObpApiException("Failed to fetch account balance: " + e.getMessage(), e);
        }
    }

    @Override
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
            throw new ObpApiException("Failed to fetch counterparties: " + e.getMessage(), e);
        }
    }

    @Override
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
            throw new ObpApiException("Failed to fetch bank ID: " + e.getMessage(), e);
        }
    }
}
