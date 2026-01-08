package com.openbanking.mcpserver.tools;

import java.util.Map;

public interface McpTool {
    String getName();
    String getDescription();
    Map<String, Object> getInputSchema();
    Object execute(Map<String, Object> params);
}
