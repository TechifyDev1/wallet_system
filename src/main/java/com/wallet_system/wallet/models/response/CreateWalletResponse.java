package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

public record CreateWalletResponse(String walletNumber, String currency, BigDecimal balance) {

}
