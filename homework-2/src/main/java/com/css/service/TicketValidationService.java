package com.css.service;

import com.css.dto.CreateTicketRequest;
import com.css.dto.UpdateTicketRequest;
import com.css.exception.ValidationException;
import com.css.model.Ticket;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class TicketValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final int SUBJECT_MIN_LENGTH = 1;
    private static final int SUBJECT_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MIN_LENGTH = 10;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    public void validateCreateRequest(CreateTicketRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (isBlank(request.getCustomerId())) {
            errors.put("customerId", "Customer ID is required");
        }

        if (isBlank(request.getCustomerEmail())) {
            errors.put("customerEmail", "Customer email is required");
        } else if (!isValidEmail(request.getCustomerEmail())) {
            errors.put("customerEmail", "Invalid email format");
        }

        if (isBlank(request.getCustomerName())) {
            errors.put("customerName", "Customer name is required");
        }

        validateSubject(request.getSubject(), errors);
        validateDescription(request.getDescription(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    public void validateUpdateRequest(UpdateTicketRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getCustomerEmail() != null && !isValidEmail(request.getCustomerEmail())) {
            errors.put("customerEmail", "Invalid email format");
        }

        if (request.getSubject() != null) {
            validateSubject(request.getSubject(), errors);
        }

        if (request.getDescription() != null) {
            validateDescription(request.getDescription(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    public Map<String, String> validateTicketData(Ticket ticket) {
        Map<String, String> errors = new HashMap<>();

        if (isBlank(ticket.getCustomerId())) {
            errors.put("customerId", "Customer ID is required");
        }

        if (isBlank(ticket.getCustomerEmail())) {
            errors.put("customerEmail", "Customer email is required");
        } else if (!isValidEmail(ticket.getCustomerEmail())) {
            errors.put("customerEmail", "Invalid email format");
        }

        if (isBlank(ticket.getCustomerName())) {
            errors.put("customerName", "Customer name is required");
        }

        validateSubject(ticket.getSubject(), errors);
        validateDescription(ticket.getDescription(), errors);

        return errors;
    }

    private void validateSubject(String subject, Map<String, String> errors) {
        if (isBlank(subject)) {
            errors.put("subject", "Subject is required");
        } else if (subject.length() < SUBJECT_MIN_LENGTH || subject.length() > SUBJECT_MAX_LENGTH) {
            errors.put("subject", "Subject must be between " + SUBJECT_MIN_LENGTH + " and " + SUBJECT_MAX_LENGTH + " characters");
        }
    }

    private void validateDescription(String description, Map<String, String> errors) {
        if (isBlank(description)) {
            errors.put("description", "Description is required");
        } else if (description.length() < DESCRIPTION_MIN_LENGTH || description.length() > DESCRIPTION_MAX_LENGTH) {
            errors.put("description", "Description must be between " + DESCRIPTION_MIN_LENGTH + " and " + DESCRIPTION_MAX_LENGTH + " characters");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}

