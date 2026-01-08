package com.openbanking.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String id;
    private String bankId;
    private String label;
    private String number;
    private String type;
    private Balance balance;
    private String iban;
    private String currency;
    private List<View> views;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {
        private String currency;
        private String amount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View {
        private String id;
        private String shortName;
        private String description;
    }
}
