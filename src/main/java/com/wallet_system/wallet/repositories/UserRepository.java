package com.wallet_system.wallet.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wallet_system.wallet.entities.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByUserName(String userName);

    Optional<UserEntity> findByUserName(String userName);

    boolean existsByPhoneNumber(
            String phoneNumber);
}
