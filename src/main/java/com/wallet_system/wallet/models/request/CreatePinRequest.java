package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePinRequest(
    @NotBlank(message = "PIN is required")
    @Size(min = 6, max = 6, message = "PIN must be exactly 6 characters")
    @Pattern(regexp = "^[0-9]*$", message = "PIN must contain only digits")
    String pin
) {}
