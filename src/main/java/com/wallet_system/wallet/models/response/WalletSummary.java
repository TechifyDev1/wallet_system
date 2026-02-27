package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

public record WalletSummary(String walletNumber, BigDecimal availableBalance, String currency, String status) {
    
}
