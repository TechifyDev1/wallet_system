package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    String newEmail,

    @NotBlank(message = "Password is required")
    String password

) {}