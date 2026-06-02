package com.openbanking.mcpserver.tools;

import com.openbanking.mcpserver.model.Account;
import com.openbanking.mcpserver.model.Balance;
import com.openbanking.mcpserver.model.Counterparty;
import com.openbanking.mcpserver.model.Transaction;
import com.openbanking.mcpserver.service.ObpApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountTools {
    
    private final ObpApiService obpApiService;
    
    public McpTool getAccountsTool() {
        return new McpTool() {
            @Override
            public String getName() {
                return "getAccounts";
            }
            
            @Override
            public String getDescription() {
                return "Retrieves all accounts for the authenticated user";
            }
            
            @Override
            public Map<String, Object> getInputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> properties = new HashMap<>();
                Map<String, Object> tokenProp = new HashMap<>();
                tokenProp.put("type", "string");
                tokenProp.put("description", "OBP authentication token");
                properties.put("token", tokenProp);
                schema.put("properties", properties);
                schema.put("required", List.of("token"));
                return schema;
            }
            
            @Override
            public Object execute(Map<String, Object> params) {
                String token = (String) params.get("token");
                if (token == null) {
                    throw new IllegalArgumentException("Token is required");
                }
                return obpApiService.getAccounts(token);
            }
        };
    }
    
    public McpTool getTransactionsTool() {
        return new McpTool() {
            @Override
            public String getName() {
                return "getTransactions";
            }
            
            @Override
            public String getDescription() {
                return "Retrieves transactions for a specific account";
            }
            
            @Override
            public Map<String, Object> getInputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> properties = new HashMap<>();
                properties.put("token", Map.of("type", "string", "description", "OBP authentication token"));
                properties.put("bankId", Map.of("type", "string", "description", "Bank ID"));
                properties.put("accountId", Map.of("type", "string", "description", "Account ID"));
                schema.put("properties", properties);
                schema.put("required", List.of("token", "bankId", "accountId"));
                return schema;
            }
            
            @Override
            public Object execute(Map<String, Object> params) {
                String token = (String) params.get("token");
                String bankId = (String) params.get("bankId");
                String accountId = (String) params.get("accountId");
                if (token == null || bankId == null || accountId == null) {
                    throw new IllegalArgumentException("Token, bankId, and accountId are required");
                }
                return obpApiService.getTransactions(token, bankId, accountId);
            }
        };
    }
    
    public McpTool getAccountBalanceTool() {
        return new McpTool() {
            @Override
            public String getName() {
                return "getAccountBalance";
            }
            
            @Override
            public String getDescription() {
                return "Retrieves the balance for a specific account";
            }
            
            @Override
            public Map<String, Object> getInputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> properties = new HashMap<>();
                properties.put("token", Map.of("type", "string", "description", "OBP authentication token"));
                properties.put("bankId", Map.of("type", "string", "description", "Bank ID"));
                properties.put("accountId", Map.of("type", "string", "description", "Account ID"));
                schema.put("properties", properties);
                schema.put("required", List.of("token", "bankId", "accountId"));
                return schema;
            }
            
            @Override
            public Object execute(Map<String, Object> params) {
                String token = (String) params.get("token");
                String bankId = (String) params.get("bankId");
                String accountId = (String) params.get("accountId");
                if (token == null || bankId == null || accountId == null) {
                    throw new IllegalArgumentException("Token, bankId, and accountId are required");
                }
                return obpApiService.getAccountBalance(token, bankId, accountId);
            }
        };
    }
    
    public McpTool getCounterpartiesTool() {
        return new McpTool() {
            @Override
            public String getName() {
                return "getCounterparties";
            }
            
            @Override
            public String getDescription() {
                return "Retrieves counterparties for a specific account";
            }
            
            @Override
            public Map<String, Object> getInputSchema() {
                Map<String, Object> schema = new HashMap<>();
                schema.put("type", "object");
                Map<String, Object> properties = new HashMap<>();
                properties.put("token", Map.of("type", "string", "description", "OBP authentication token"));
                properties.put("bankId", Map.of("type", "string", "description", "Bank ID"));
                properties.put("accountId", Map.of("type", "string", "description", "Account ID"));
                schema.put("properties", properties);
                schema.put("required", List.of("token", "bankId", "accountId"));
                return schema;
            }
            
            @Override
            public Object execute(Map<String, Object> params) {
                String token = (String) params.get("token");
                String bankId = (String) params.get("bankId");
                String accountId = (String) params.get("accountId");
                if (token == null || bankId == null || accountId == null) {
                    throw new IllegalArgumentException("Token, bankId, and accountId are required");
                }
                return obpApiService.getCounterparties(token, bankId, accountId);
            }
        };
    }
}
