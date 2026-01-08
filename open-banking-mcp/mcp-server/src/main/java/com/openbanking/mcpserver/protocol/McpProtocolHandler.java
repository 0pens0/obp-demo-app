package com.openbanking.mcpserver.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpProtocolHandler {
    
    private final ObjectMapper objectMapper;
    
    public McpRequest parseRequest(String json) {
        try {
            return objectMapper.readValue(json, McpRequest.class);
        } catch (Exception e) {
            log.error("Error parsing MCP request: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid MCP request format", e);
        }
    }
    
    public String formatResponse(McpResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error formatting MCP response: {}", e.getMessage(), e);
            return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }
}
