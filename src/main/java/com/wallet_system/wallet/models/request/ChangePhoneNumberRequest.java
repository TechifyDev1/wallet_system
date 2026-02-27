package com.wallet_system.wallet.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePhoneNumberRequest(

    @NotBlank(message = "New phone number is required")
    @Pattern(
        regexp = "^[0-9]{10,15}$",
        message = "Phone number must be 10 to 15 digits"
    )
    String newPhoneNumber,

    @NotBlank(message = "Password is required")
    String password

) {}