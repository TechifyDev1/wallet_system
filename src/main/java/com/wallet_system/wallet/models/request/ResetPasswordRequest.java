package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank (message = "Current password is required") String currentPassword,
        @NotBlank(message = "New password is required") String newPassword) {
    
}
