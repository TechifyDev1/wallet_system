package com.wallet_system.wallet.models.response;

public record SecurityProfile(boolean hasTransactionPin, boolean isKycVerified, String kycLevel,
        boolean twoFactorEnabled, String accountStatus) {

}
