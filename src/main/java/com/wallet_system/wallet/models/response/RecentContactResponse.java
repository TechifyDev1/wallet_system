package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentContactResponse(String userName, LocalDateTime lastTransactionDate, BigDecimal amount, String profilePicUrl) {
    
}
