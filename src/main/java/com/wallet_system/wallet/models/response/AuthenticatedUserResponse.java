package com.wallet_system.wallet.models.response;

public record AuthenticatedUserResponse(UserProfile user, WalletSummary wallet, SecurityProfile security, String profilePicUrl) {

}
