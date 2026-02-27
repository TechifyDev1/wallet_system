package com.wallet_system.wallet.models.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChangePhoneNumberResponse(

    UUID userId,
    String oldPhoneNumber,
    String newPhoneNumber,
    LocalDateTime updatedAt,
    String message

) {}