package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email address") String email,
        @NotBlank(message = "Username is required") String userName,
        @NotBlank(message = "Phone number is required") String phoneNumber,
        @NotBlank(message = "Password is required") String password) {
}
