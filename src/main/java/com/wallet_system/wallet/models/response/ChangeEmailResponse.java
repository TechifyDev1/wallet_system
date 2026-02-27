package com.wallet_system.wallet.models.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeEmailResponse(

    UUID userId,
    String oldEmail,
    String newEmail,
    LocalDateTime updatedAt,
    String message

) {}