package com.wallet_system.wallet.exceptions;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Double amountRequested, Double currentBalance) {
        super(String.format("Insufficient funds: you tried to spend ₦%.2f, but only have ₦%.2f", amountRequested, currentBalance));
    }
}
