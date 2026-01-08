package com.obp.demo.service;

import com.obp.demo.model.Account;
import com.obp.demo.model.Counterparty;
import com.obp.demo.model.Transaction;

import java.util.List;

public interface ObpApiService {
    String createUser(String username, String email, String password, String firstName, String lastName);
    String authenticateUser(String username, String password);
    List<Account> getAccounts(String token);
    List<Transaction> getTransactions(String token, String bankId, String accountId);
    List<Counterparty> getCounterparties(String token, String bankId, String accountId);
    String getBankId(String token);
}
