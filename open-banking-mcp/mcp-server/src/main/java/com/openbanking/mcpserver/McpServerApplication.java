package com.openbanking.mcpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openbanking.mcpserver.tools.AccountTools;
import com.openbanking.mcpserver.tools.McpTool;
import com.openbanking.mcpserver.tools.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initializeTools(ToolRegistry toolRegistry, AccountTools accountTools) {
        return args -> {
            log.info("Registering MCP tools...");
            toolRegistry.registerTool("getAccounts", accountTools.getAccountsTool());
            toolRegistry.registerTool("getTransactions", accountTools.getTransactionsTool());
            toolRegistry.registerTool("getAccountBalance", accountTools.getAccountBalanceTool());
            toolRegistry.registerTool("getCounterparties", accountTools.getCounterpartiesTool());
            log.info("MCP tools registered successfully");
        };
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
