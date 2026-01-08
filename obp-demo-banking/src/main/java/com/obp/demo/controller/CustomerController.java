package com.obp.demo.controller;

import com.obp.demo.dto.LoginRequest;
import com.obp.demo.model.Account;
import com.obp.demo.model.Counterparty;
import com.obp.demo.model.CustomerDashboard;
import com.obp.demo.model.Transaction;
import com.obp.demo.service.ObpApiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final ObpApiService obpApiService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "customer/login";
    }

    @PostMapping("/authenticate")
    public String authenticate(@Valid LoginRequest loginRequest,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/login";
        }

        try {
            String token = obpApiService.authenticateUser(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
            );

            // Store token in session
            session.setAttribute("obpToken", token);
            session.setAttribute("username", loginRequest.getUsername());

            // Set Spring Security authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            log.error("Authentication failed", e);
            redirectAttributes.addFlashAttribute("error", 
                "Invalid credentials. Please check your username and password.");
            return "redirect:/customer/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("obpToken");
        String username = (String) session.getAttribute("username");

        if (token == null) {
            return "redirect:/customer/login";
        }

        try {
            CustomerDashboard dashboard = new CustomerDashboard();
            dashboard.setUsername(username);

            // Get accounts
            List<Account> accounts = obpApiService.getAccounts(token);
            dashboard.setAccounts(accounts);

            // Get transactions and counterparties from first account
            if (!accounts.isEmpty()) {
                String bankId = obpApiService.getBankId(token);
                if (bankId != null) {
                    String accountId = accounts.get(0).getId();
                    
                    // Get transactions
                    List<Transaction> allTransactions = obpApiService.getTransactions(
                            token, bankId, accountId);
                    // Limit to recent 10 transactions
                    dashboard.setRecentTransactions(
                            allTransactions.stream().limit(10).collect(Collectors.toList()));
                    
                    // Get counterparties
                    List<Counterparty> counterparties = obpApiService.getCounterparties(
                            token, bankId, accountId);
                    dashboard.setCounterparties(counterparties);
                }
            }

            // Calculate total balance
            if (!accounts.isEmpty() && accounts.get(0).getBalance() != null) {
                dashboard.setTotalBalance(accounts.get(0).getBalance().getAmount());
                dashboard.setCurrency(accounts.get(0).getBalance().getCurrency());
            }

            model.addAttribute("dashboard", dashboard);
            return "customer/dashboard";
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to load dashboard data. Please try again.");
            return "redirect:/customer/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/customer/login";
    }
}
