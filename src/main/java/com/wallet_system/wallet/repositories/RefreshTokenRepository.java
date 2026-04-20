package com.wallet_system.wallet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.wallet_system.wallet.entities.RefreshToken;
import com.wallet_system.wallet.entities.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    

    @Modifying
    @Transactional
    void deleteByUser(UserEntity user);
}
