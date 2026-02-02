package com.banking.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces
@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.toList());
        return HttpResponse.badRequest(Map.of("errors", errors));
    }
}
