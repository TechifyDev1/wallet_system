package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

public record FundWalletResponse(
    String message,
    String reference,
    BigDecimal amount,
    BigDecimal newBalance,
    String status
) {}