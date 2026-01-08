package com.openbanking.mcpserver.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpRequest {
    private String jsonrpc = "2.0";
    private String method;
    private String id;
    private Map<String, Object> params;
}
