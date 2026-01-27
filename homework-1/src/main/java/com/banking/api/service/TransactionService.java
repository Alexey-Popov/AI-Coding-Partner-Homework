package com.banking.api.service;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.dto.TransactionResponse;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .type(request.getType())
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }
    
    public TransactionResponse getTransactionById(String id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return mapToResponse(transaction);
    }
    
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TransactionResponse> getFilteredTransactions(String accountId, TransactionType type,
                                                             LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findWithFilters(accountId, type, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .fromAccount(transaction.getFromAccount())
                .toAccount(transaction.getToAccount())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .build();
    }
}
