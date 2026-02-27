package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

import com.wallet_system.wallet.enums.EntryType;
import com.wallet_system.wallet.enums.TransactionStatus;

public record TransferResponse(String receiverUsername, TransactionStatus status, EntryType type, BigDecimal amount, String message, String reference) {
}