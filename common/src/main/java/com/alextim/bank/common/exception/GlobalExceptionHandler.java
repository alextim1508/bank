package com.alextim.bank.common.exception;

import com.alextim.bank.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Constraint violation failed", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        });

        errors.forEach((field, messages) -> messages.sort(String::compareTo));

        Map<String, List<String>> sortedErrors = errors.entrySet().stream()
                .sorted(
                        Map.Entry.<String, List<String>>comparingByKey()
                                .thenComparing(Map.Entry::getKey, Comparator.comparingInt(String::length))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        ApiResponse<Map<String, List<String>>> response = ApiResponse.error("Validation failed", sortedErrors.toString());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ServiceClientException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceClientExceptions(ServiceClientException ex) {
        log.error("handleServiceClientExceptions", ex);

        return ResponseEntity.badRequest().body(ApiResponse.error("Internal Server Error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidTokenException(InvalidTokenException ex) {
        log.error("handleInvalidTokenException", ex);

        return ResponseEntity.badRequest().body(ApiResponse.error( "Invalid dto", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.error("handleAccessDenied: {}", ex.getMessage());

        return ResponseEntity.status(403).body(ApiResponse.error( "Access denied", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<?>>  handleUnauthenticated(AuthenticationCredentialsNotFoundException ex) {
        log.error("handleUnauthenticated: {}", ex.getMessage());

        return ResponseEntity.status(401).body(ApiResponse.error( "Unauthenticated", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(RuntimeException ex) {
        log.error("handleAllExceptions", ex);

        return ResponseEntity.internalServerError().body(ApiResponse.error("Internal Server Error", ex.getMessage()));
    }
}
