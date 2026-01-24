package com.banking.transactions.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CurrencyValidator {
    
    // ISO 4217 currency codes - most popular currencies
    private static final Set<String> VALID_CURRENCIES = new HashSet<>(Arrays.asList(
        // Major currencies
        "USD", // US Dollar
        "EUR", // Euro
        "GBP", // British Pound Sterling
        "JPY", // Japanese Yen
        "CHF", // Swiss Franc
        "CAD", // Canadian Dollar
        "AUD", // Australian Dollar
        "NZD", // New Zealand Dollar
        
        // Asian currencies
        "CNY", // Chinese Yuan
        "HKD", // Hong Kong Dollar
        "SGD", // Singapore Dollar
        "KRW", // South Korean Won
        "INR", // Indian Rupee
        "THB", // Thai Baht
        "MYR", // Malaysian Ringgit
        "IDR", // Indonesian Rupiah
        "PHP", // Philippine Peso
        "VND", // Vietnamese Dong
        
        // European currencies
        "SEK", // Swedish Krona
        "NOK", // Norwegian Krone
        "DKK", // Danish Krone
        "PLN", // Polish Zloty
        "CZK", // Czech Koruna
        "HUF", // Hungarian Forint
        "RON", // Romanian Leu
        "BGN", // Bulgarian Lev
        "HRK", // Croatian Kuna
        "RUB", // Russian Ruble
        "TRY", // Turkish Lira
        "UAH", // Ukrainian Hryvnia
        
        // Middle East & Africa
        "AED", // UAE Dirham
        "SAR", // Saudi Riyal
        "QAR", // Qatari Riyal
        "KWD", // Kuwaiti Dinar
        "BHD", // Bahraini Dinar
        "OMR", // Omani Rial
        "JOD", // Jordanian Dinar
        "ILS", // Israeli Shekel
        "EGP", // Egyptian Pound
        "ZAR", // South African Rand
        "NGN", // Nigerian Naira
        "KES", // Kenyan Shilling
        
        // Americas
        "MXN", // Mexican Peso
        "BRL", // Brazilian Real
        "ARS", // Argentine Peso
        "CLP", // Chilean Peso
        "COP", // Colombian Peso
        "PEN", // Peruvian Sol
        
        // Others
        "PKR", // Pakistani Rupee
        "BDT", // Bangladeshi Taka
        "LKR"  // Sri Lankan Rupee
    ));
    
    public static boolean isValidCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return false;
        }
        // ISO 4217 requires exact uppercase match
        return VALID_CURRENCIES.contains(currencyCode);
    }
    
    public static Set<String> getValidCurrencies() {
        return new HashSet<>(VALID_CURRENCIES);
    }
}
