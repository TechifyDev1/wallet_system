package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank(message = "Email is required") String email, 
        @NotBlank(message = "Secret key is required") String secretKey, @NotBlank(message = "New password is required") String newPassword) {
    
}