package com.openbanking.mcpserver.tools;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class ToolRegistry {
    
    private final Map<String, McpTool> tools = new HashMap<>();
    
    public void registerTool(String name, McpTool tool) {
        tools.put(name, tool);
    }
    
    public McpTool getTool(String name) {
        return tools.get(name);
    }
}
