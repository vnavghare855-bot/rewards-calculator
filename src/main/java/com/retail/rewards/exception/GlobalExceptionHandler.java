package com.retail.rewards.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler to capture and format API errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all generic exceptions and returns an Internal Server Error response.
     *
     * @param ex the generic exception
     * @return a response entity containing the error message and status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.internalServerError().body(error);
    }

    /**
     * Handles invalid arguments and bad requests, returning a Bad Request response.
     *
     * @param ex the IllegalArgumentException
     * @return a response entity containing the bad request error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
