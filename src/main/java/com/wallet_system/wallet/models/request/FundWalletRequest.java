package com.wallet_system.wallet.models.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FundWalletRequest(

        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") @Digits(integer = 15, fraction = 4) 
        @JsonProperty("amount") BigDecimal amount,

        @NotBlank(message = "Idempotency key is required") 
        @JsonProperty("idempotencyKey") String idempotencyKey) {
}