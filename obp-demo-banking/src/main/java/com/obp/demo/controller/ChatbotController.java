package com.obp.demo.controller;

import com.obp.demo.dto.ChatMessage;
import com.obp.demo.model.CustomerDashboard;
import com.obp.demo.service.ChatbotService;
import com.obp.demo.service.ObpApiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ObpApiService obpApiService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request,
                                                     HttpSession session) {
        String userMessage = request.get("message");
        String token = (String) session.getAttribute("obpToken");

        if (token == null) {
            return ResponseEntity.ok(Map.of("response", 
                "Please log in to use the chatbot."));
        }

        try {
            // Build dashboard context for the chatbot
            CustomerDashboard dashboard = new CustomerDashboard();
            dashboard.setUsername((String) session.getAttribute("username"));

            List<com.obp.demo.model.Account> accounts = obpApiService.getAccounts(token);
            dashboard.setAccounts(accounts);

            if (!accounts.isEmpty()) {
                String bankId = obpApiService.getBankId(token);
                if (bankId != null) {
                    List<com.obp.demo.model.Transaction> transactions = 
                        obpApiService.getTransactions(token, bankId, accounts.get(0).getId());
                    dashboard.setRecentTransactions(
                        transactions.stream().limit(10).collect(Collectors.toList()));

                    List<com.obp.demo.model.Counterparty> counterparties = 
                        obpApiService.getCounterparties(token, bankId, accounts.get(0).getId());
                    dashboard.setCounterparties(counterparties);
                }
            }

            String username = (String) session.getAttribute("username");
            String response = chatbotService.getChatResponse(userMessage, dashboard, username != null ? username : "anonymous");
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.ok(Map.of("response", 
                "I apologize, but I'm having trouble processing your request right now."));
        }
    }
}
