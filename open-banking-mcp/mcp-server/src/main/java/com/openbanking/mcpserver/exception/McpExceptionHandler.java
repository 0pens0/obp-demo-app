package com.openbanking.mcpserver.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class McpExceptionHandler {
    
    @ExceptionHandler(ObpApiException.class)
    public ResponseEntity<Map<String, Object>> handleObpApiException(ObpApiException ex) {
        log.error("OBP API error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                    "error", true,
                    "message", "Error communicating with OBP API: " + ex.getMessage(),
                    "code", "OBP_API_ERROR"
                ));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", true,
                    "message", ex.getMessage(),
                    "code", "INVALID_ARGUMENT"
                ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", true,
                    "message", "An unexpected error occurred: " + ex.getMessage(),
                    "code", "INTERNAL_ERROR"
                ));
    }
}
