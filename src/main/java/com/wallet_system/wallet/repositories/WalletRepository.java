package com.wallet_system.wallet.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.entities.WalletEntity;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Boolean existsByWalletNumber(String walletNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<WalletEntity> findByUser(UserEntity user);
}
