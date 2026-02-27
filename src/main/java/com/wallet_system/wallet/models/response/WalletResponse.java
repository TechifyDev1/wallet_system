package com.wallet_system.wallet.models.response;

import java.math.BigDecimal;

public class WalletResponse {
    private String walletNumber;
    private BigDecimal balance;
    private String currency;
    public String getWalletNumber() {
        return walletNumber;
    }
    public void setWalletNumber(String walletNumber) {
        this.walletNumber = walletNumber;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
