package com.banking.api.controller;

import com.banking.api.dto.BalanceResponse;
import com.banking.api.dto.InterestResponse;
import com.banking.api.dto.SummaryResponse;
import com.banking.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        BalanceResponse response = accountService.getBalance(accountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}/summary")
    public ResponseEntity<SummaryResponse> getAccountSummary(@PathVariable String accountId) {
        SummaryResponse response = accountService.getAccountSummary(accountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}/interest")
    public ResponseEntity<InterestResponse> calculateInterest(
            @PathVariable String accountId,
            @RequestParam double rate,
            @RequestParam int days) {
        
        InterestResponse response = accountService.calculateInterest(accountId, rate, days);
        return ResponseEntity.ok(response);
    }
}
