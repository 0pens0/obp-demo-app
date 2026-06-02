package com.openbanking.mcpserver.controller;

import com.openbanking.mcpserver.protocol.McpProtocolHandler;
import com.openbanking.mcpserver.protocol.McpRequest;
import com.openbanking.mcpserver.protocol.McpResponse;
import com.openbanking.mcpserver.tools.McpTool;
import com.openbanking.mcpserver.tools.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@Slf4j
public class McpController {
    
    private final ToolRegistry toolRegistry;
    private final McpProtocolHandler protocolHandler;
    
    @PostMapping("/call")
    public ResponseEntity<McpResponse> callTool(@RequestBody McpRequest request) {
        try {
            if ("tools/list".equals(request.getMethod())) {
                List<Map<String, Object>> tools = toolRegistry.getTools().values().stream()
                        .map(tool -> {
                            Map<String, Object> toolDef = new HashMap<>();
                            toolDef.put("name", tool.getName());
                            toolDef.put("description", tool.getDescription());
                            toolDef.put("inputSchema", tool.getInputSchema());
                            return toolDef;
                        })
                        .collect(Collectors.toList());
                return ResponseEntity.ok(McpResponse.success(request.getId(), Map.of("tools", tools)));
            }
            
            if ("tools/call".equals(request.getMethod())) {
                Map<String, Object> params = request.getParams();
                String toolName = (String) params.get("name");
                Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                
                McpTool tool = toolRegistry.getTool(toolName);
                if (tool == null) {
                    return ResponseEntity.ok(McpResponse.error(request.getId(), -32601, "Tool not found: " + toolName));
                }
                
                try {
                    Object result = tool.execute(arguments);
                    return ResponseEntity.ok(McpResponse.success(request.getId(), result));
                } catch (Exception e) {
                    log.error("Error executing tool {}: {}", toolName, e.getMessage(), e);
                    return ResponseEntity.ok(McpResponse.error(request.getId(), -32603, "Tool execution error: " + e.getMessage()));
                }
            }
            
            return ResponseEntity.ok(McpResponse.error(request.getId(), -32601, "Method not found: " + request.getMethod()));
        } catch (Exception e) {
            log.error("Error processing MCP request: {}", e.getMessage(), e);
            return ResponseEntity.ok(McpResponse.error(request.getId() != null ? request.getId() : "unknown", -32603, "Internal error: " + e.getMessage()));
        }
    }
}
