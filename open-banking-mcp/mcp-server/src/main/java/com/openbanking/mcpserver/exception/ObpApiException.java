package com.openbanking.mcpserver.exception;

public class ObpApiException extends RuntimeException {
    
    public ObpApiException(String message) {
        super(message);
    }
    
    public ObpApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
