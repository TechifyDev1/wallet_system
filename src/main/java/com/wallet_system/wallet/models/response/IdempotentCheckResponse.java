package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

public record IdempotentCheckResponse(String message, String reference, BigDecimal amount, String status) {

}
