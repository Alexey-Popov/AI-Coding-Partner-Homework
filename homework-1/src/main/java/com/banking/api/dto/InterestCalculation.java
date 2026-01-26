package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Interest Calculation Response
 * Contains interest calculation details using simple interest formula
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestCalculation {
    
    /**
     * Account ID for which interest is calculated
     */
    private String accountId;
    
    /**
     * Current balance (principal amount)
     */
    private BigDecimal currentBalance;
    
    /**
     * Annual interest rate (as decimal, e.g., 0.05 for 5%)
     */
    private BigDecimal interestRate;
    
    /**
     * Number of days for interest calculation
     */
    private Integer days;
    
    /**
     * Calculated interest amount
     */
    private BigDecimal interestAmount;
    
    /**
     * Projected balance after adding interest
     */
    private BigDecimal projectedBalance;
    
    /**
     * Formula used for calculation (for transparency)
     */
    private String formula;
    
    /**
     * Currency code
     */
    private String currency;
}
