package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wallet_system.wallet.enums.EntryType;
import com.wallet_system.wallet.enums.TransactionStatus;

public record TransactionsResponse(String systemDescription, BigDecimal amount, LocalDateTime time,
        EntryType entryType, TransactionStatus status, String reference, String note) {
}