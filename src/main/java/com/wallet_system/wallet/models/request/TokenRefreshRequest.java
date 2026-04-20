package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {
    
}
