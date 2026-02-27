package com.wallet_system.wallet.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wallet_system.wallet.entities.LedgerEntity;
import com.wallet_system.wallet.entities.UserEntity;

public interface LedgerRepository extends JpaRepository<LedgerEntity, UUID> {

    @EntityGraph(attributePaths = {"transaction"})
    Page<LedgerEntity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
}
