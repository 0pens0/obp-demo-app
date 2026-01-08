package com.openbanking.mcpserver.service;

import com.openbanking.mcpserver.model.Account;
import com.openbanking.mcpserver.model.Balance;
import com.openbanking.mcpserver.model.Counterparty;
import com.openbanking.mcpserver.model.Transaction;

import java.util.List;

public interface ObpApiService {
    List<Account> getAccounts(String token);
    List<Transaction> getTransactions(String token, String bankId, String accountId);
    Balance getAccountBalance(String token, String bankId, String accountId);
    List<Counterparty> getCounterparties(String token, String bankId, String accountId);
    String getBankId(String token);
}
