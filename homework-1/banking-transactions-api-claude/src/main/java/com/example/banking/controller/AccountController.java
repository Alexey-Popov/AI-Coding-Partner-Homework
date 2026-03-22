package com.example.banking.controller;

import com.example.banking.dto.AccountBalanceResponse;
import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable String accountId) {
        AccountBalanceResponse balance = accountService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountId}/summary")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(@PathVariable String accountId) {
        AccountSummaryResponse summary = accountService.getAccountSummary(accountId);
        return ResponseEntity.ok(summary);
    }
}
