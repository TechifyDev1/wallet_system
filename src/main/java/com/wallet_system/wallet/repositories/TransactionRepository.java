package com.wallet_system.wallet.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wallet_system.wallet.entities.TransactionEntity;
import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.enums.TransactionType;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);
    List<TransactionEntity> findTop20ByUserAndTypeOrderByCreatedAtDesc(UserEntity user, TransactionType type);
}
