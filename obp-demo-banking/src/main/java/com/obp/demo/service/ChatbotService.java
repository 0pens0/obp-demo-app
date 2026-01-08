package com.obp.demo.service;

import com.obp.demo.model.CustomerDashboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatClient chatClient;

    public String getChatResponse(String userMessage, CustomerDashboard dashboard) {
        try {
            String context = buildContext(dashboard);
            
            String prompt = String.format(
                "You are a helpful banking assistant. Answer questions about the customer's banking information. " +
                "Here is the customer's current banking data:\n\n%s\n\n" +
                "Customer question: %s\n\n" +
                "Provide a helpful and concise answer. If the question is not related to banking or the customer's data, politely redirect.",
                context,
                userMessage
            );

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Error getting chat response: {}", e.getMessage(), e);
            return "I apologize, but I'm having trouble processing your request right now. Please try again later.";
        }
    }

    private String buildContext(CustomerDashboard dashboard) {
        StringBuilder context = new StringBuilder();
        
        if (dashboard.getAccounts() != null && !dashboard.getAccounts().isEmpty()) {
            context.append("Accounts:\n");
            for (var account : dashboard.getAccounts()) {
                context.append(String.format("- %s (%s): %s %s\n", 
                    account.getLabel() != null ? account.getLabel() : account.getNumber(),
                    account.getType(),
                    account.getBalance() != null ? account.getBalance().getAmount() : "0",
                    account.getBalance() != null ? account.getBalance().getCurrency() : "USD"));
            }
        }
        
        if (dashboard.getRecentTransactions() != null && !dashboard.getRecentTransactions().isEmpty()) {
            context.append("\nRecent Transactions:\n");
            int count = Math.min(5, dashboard.getRecentTransactions().size());
            for (int i = 0; i < count; i++) {
                var tx = dashboard.getRecentTransactions().get(i);
                context.append(String.format("- %s: %s %s - %s\n",
                    tx.getDescription() != null ? tx.getDescription() : "Transaction",
                    tx.getValue() != null ? tx.getValue().getAmount() : "0",
                    tx.getValue() != null ? tx.getValue().getCurrency() : "USD",
                    tx.getPosted() != null ? tx.getPosted() : ""));
            }
        }
        
        if (dashboard.getCounterparties() != null && !dashboard.getCounterparties().isEmpty()) {
            context.append("\nCounterparties:\n");
            for (var cp : dashboard.getCounterparties()) {
                context.append(String.format("- %s\n", cp.getName()));
            }
        }
        
        return context.toString();
    }
}
