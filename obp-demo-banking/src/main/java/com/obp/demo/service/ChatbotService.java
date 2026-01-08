package com.obp.demo.service;

import com.obp.demo.model.CustomerDashboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatClient chatClient;
    private final VectorStoreService vectorStoreService;

    public String getChatResponse(String userMessage, CustomerDashboard dashboard, String username) {
        try {
            // Retrieve similar context from vector store (RAG)
            List<Document> similarDocs = vectorStoreService.searchSimilar(userMessage, 3);
            String retrievedContext = similarDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n"));
            
            String currentContext = buildContext(dashboard);
            
            // Combine retrieved context with current dashboard context
            String fullContext = retrievedContext.isEmpty() 
                ? currentContext 
                : "Previous similar conversations:\n" + retrievedContext + "\n\nCurrent account information:\n" + currentContext;
            
            String prompt = String.format(
                "You are a helpful banking assistant. Answer questions about the customer's banking information. " +
                "Use the following context to provide accurate and helpful answers:\n\n%s\n\n" +
                "Customer question: %s\n\n" +
                "Provide a helpful and concise answer. If the question is not related to banking or the customer's data, politely redirect.",
                fullContext,
                userMessage
            );

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            // Store chat history in vector store for future RAG
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("username", username);
            metadata.put("timestamp", System.currentTimeMillis());
            vectorStoreService.addChatHistory(username, userMessage, response, metadata);
            
            return response;
        } catch (Exception e) {
            log.error("Error getting chat response: {}", e.getMessage(), e);
            return "I apologize, but I'm having trouble processing your request right now. Please try again later.";
        }
    }
    
    public String getChatResponse(String userMessage, CustomerDashboard dashboard) {
        return getChatResponse(userMessage, dashboard, "anonymous");
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
