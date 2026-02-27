package com.wallet_system.wallet.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.entities.WalletEntity;
import com.wallet_system.wallet.enums.Currency;
import com.wallet_system.wallet.enums.WalletStatus;
import com.wallet_system.wallet.models.response.CreateWalletResponse;
import com.wallet_system.wallet.repositories.WalletRepository;

@Service
public class WalletService {

    final private WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public CreateWalletResponse create(UserEntity user) {
        WalletEntity wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setWalletNumber(generateWalletNumber());
        wallet.setCurrency(Currency.NGN);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);
        CreateWalletResponse response = new CreateWalletResponse(wallet.getWalletNumber(), wallet.getCurrency().name(), wallet.getBalance());
        return response;
    }

    private String generateWalletNumber() {
        String number;
        do {
            number = String.valueOf(
                ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L)
            );
        } while (walletRepository.existsByWalletNumber(number));
        return number;
    }
}
