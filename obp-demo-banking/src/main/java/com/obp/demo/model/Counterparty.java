package com.obp.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Counterparty {
    private String counterpartyId;
    private String name;
    private String accountNumber;
    private String accountRoutingScheme;
    private String accountRoutingAddress;
    private String bankRoutingScheme;
    private String bankRoutingAddress;
}
