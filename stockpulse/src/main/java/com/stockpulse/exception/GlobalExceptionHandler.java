package com.stockpulse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Optimistic Locking Conflict
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleConcurrencyConflict(ObjectOptimisticLockingFailureException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "CONFLICT");
        error.put("message", "Stock was just updated by another buyer. Please retry!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 2. Business Logic Errors (Out of stock ya Expired order)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        if (ex.getMessage() != null && ex.getMessage().startsWith("EXPIRED")) {
            error.put("error", "GONE");
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.GONE).body(error);
        }
        error.put("error", "BAD_REQUEST");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 3. Invalid ID (Product ya Order nahi mila)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "NOT_FOUND");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 4. DTO Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> error = new HashMap<>();
        String firstErrorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        error.put("error", "VALIDATION_FAILED");
        error.put("message", firstErrorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}