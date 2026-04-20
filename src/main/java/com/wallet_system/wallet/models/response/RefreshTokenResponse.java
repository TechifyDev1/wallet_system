package com.wallet_system.wallet.models.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponse(
    @JsonProperty("token") String token,
    @JsonProperty("expiresAt") Instant expiresAt
) {
}
