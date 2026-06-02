package com.openbanking.mcpserver.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpResponse {
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private McpError error;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpError {
        private int code;
        private String message;
        private Object data;
    }
    
    public static McpResponse success(String id, Object result) {
        return new McpResponse("2.0", id, result, null);
    }
    
    public static McpResponse error(String id, int code, String message) {
        return new McpResponse("2.0", id, null, new McpError(code, message, null));
    }
}
