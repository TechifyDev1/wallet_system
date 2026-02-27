package com.wallet_system.wallet.models.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfile(UUID id, String firstName, String lastName, String userName, String email,
        String phoneNumber, LocalDateTime createdAt, String profilePicUrl) {

}
