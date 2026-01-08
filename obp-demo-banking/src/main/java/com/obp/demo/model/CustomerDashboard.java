package com.obp.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboard {
    private String username;
    private List<Account> accounts;
    private List<Transaction> recentTransactions;
    private List<Counterparty> counterparties;
    private String totalBalance;
    private String currency;
}
