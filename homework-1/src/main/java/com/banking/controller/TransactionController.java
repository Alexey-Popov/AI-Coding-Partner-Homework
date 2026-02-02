package com.banking.controller;

import com.banking.model.Transaction;
import com.banking.service.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@Validated
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Post("/transactions")
    public HttpResponse<?> create(@Body @Valid Transaction transaction) {
        List<String> errors = service.validate(transaction);
        if (!errors.isEmpty()) {
            return HttpResponse.badRequest(Map.of("errors", errors));
        }
        Transaction created = service.create(transaction);
        return HttpResponse.created(created);
    }

    @Get("/transactions")
    public List<Transaction> list(
            @QueryValue(defaultValue = "") String accountId,
            @QueryValue(defaultValue = "") String type,
            @QueryValue(defaultValue = "") String from,
            @QueryValue(defaultValue = "") String to) {
        String acct = accountId.isEmpty() ? null : accountId;
        String tp = type.isEmpty() ? null : type;
        LocalDateTime fromDt = from.isEmpty() ? null : LocalDateTime.parse(from);
        LocalDateTime toDt = to.isEmpty() ? null : LocalDateTime.parse(to);
        return service.findAll(acct, tp, fromDt, toDt);
    }

    @Get("/transactions/{id}")
    public HttpResponse<Transaction> getById(@PathVariable String id) {
        return service.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get("/accounts/{accountId}/balance")
    public Map<String, Object> getBalance(@PathVariable String accountId) {
        return Map.of("accountId", accountId, "balance", service.getBalance(accountId));
    }

    @Get("/accounts/{accountId}/summary")
    public Map<String, Object> getSummary(@PathVariable String accountId) {
        return service.getSummary(accountId);
    }
}
