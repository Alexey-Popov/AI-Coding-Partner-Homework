package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestResponse {
    
    private String accountId;
    private BigDecimal principal;
    private double rate;
    private int days;
    private BigDecimal interest;
    private BigDecimal totalAmount;
}
