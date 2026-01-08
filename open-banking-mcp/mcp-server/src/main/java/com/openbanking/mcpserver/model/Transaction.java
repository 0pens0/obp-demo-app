package com.openbanking.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String id;
    private String accountId;
    private String transactionType;
    private TransactionValue value;
    private String description;
    private String posted;
    private String completed;
    private Counterparty counterparty;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionValue {
        private String currency;
        private String amount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Counterparty {
        private String name;
        private String accountId;
    }
}
