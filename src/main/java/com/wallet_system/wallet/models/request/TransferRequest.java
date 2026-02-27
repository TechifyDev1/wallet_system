package com.wallet_system.wallet.models.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(@NotNull BigDecimal amount, @NotBlank(message = "Reciver's username must be provided") String receiverUsername, @NotBlank(message = "Idempotency key can't be blank") String idempotencyKey, String comment, @NotBlank(message = "Pin cannot be empty") String transactionPin) {
    
}
