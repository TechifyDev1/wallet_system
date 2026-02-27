package com.wallet_system.wallet.exceptions;

import java.net.URI;
import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle "Wrong username and password input:"
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Invalid email or password");
        problemDetail.setTitle("Authentication failed");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("https://wallet.com"));
        return problemDetail;
    }

    // 2. Handle "Not Found" errors
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 3. Catch-all for everything else (Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        ex.printStackTrace();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
        problem.setTitle("Server Error");
        return problem;
    }

    @ExceptionHandler({
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ProblemDetail handleBadRequestExceptions(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("https://wallet.com"));
        return problemDetail;
    }

    // 4. Catch insufficient funds exception
    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientBalanceException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, // Or 422 (UNPROCESSABLE_ENTITY)
                exception.getMessage());
        problemDetail.setTitle("Transaction Failed");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("error_code", "WALLET_LOW_BALANCE");
        problemDetail.setType(URI.create("https://wallet.com"));
        return problemDetail;
    }

    // 5. Catch user already exists exception
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Registration Conflict");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("https://wallet.com"));
        return problemDetail;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Unauthorized request");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("https://wallet.com"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Get the first error message specifically (e.g., "Phone number is required")
        String specificMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                specificMessage);

        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String rootMsg = ex.getMostSpecificCause().getMessage();
        String specificDetail = "A record with this information already exists.";

        // Logic to pinpoint the exact duplicate field
        if (rootMsg != null) {
            if (rootMsg.contains("phone_number")) {
                specificDetail = "This phone number is already registered.";
            } else if (rootMsg.contains("email")) {
                specificDetail = "This email address is already in use.";
            } else if (rootMsg.contains("user_name")) {
                specificDetail = "This username is already taken.";
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                specificDetail 
        );

        problemDetail.setTitle("Duplicate Information");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

}
