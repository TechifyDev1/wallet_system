package com.wallet_system.wallet.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.entities.WalletEntity;
import com.wallet_system.wallet.exceptions.ResourceNotFoundException;
import com.wallet_system.wallet.models.request.ChangeEmailRequest;
import com.wallet_system.wallet.models.request.ChangePhoneNumberRequest;
import com.wallet_system.wallet.models.response.AuthenticatedUserResponse;
import com.wallet_system.wallet.models.response.ChangeEmailResponse;
import com.wallet_system.wallet.models.response.ChangePhoneNumberResponse;
import com.wallet_system.wallet.models.response.RecentContactResponse;
import com.wallet_system.wallet.models.response.SecurityProfile;
import com.wallet_system.wallet.models.response.UserProfile;
import com.wallet_system.wallet.models.response.WalletSummary;
import com.wallet_system.wallet.repositories.UserRepository;
import com.wallet_system.wallet.repositories.WalletRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

        private final AuthService authService;
        private final WalletRepository walletRepository;
        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;

        public UserService(UserRepository userRepository, AuthService authService, WalletRepository walletRepository,
                        PasswordEncoder passwordEncoder) {
                this.authService = authService;
                this.walletRepository = walletRepository;
                this.passwordEncoder = passwordEncoder;
                this.userRepository = userRepository;
        }

        @Transactional
        public AuthenticatedUserResponse getAuthenticatedUser() {
                UserEntity user = authService.getAuthenticatedUser();
                UserProfile profile = new UserProfile(user.getId(), user.getFirstName(), user.getLastName(),
                                user.getUserName(),
                                user.getEmail(), user.getPhoneNumber(), user.getCreatedAt(), user.getProfilePicUrl());
                WalletEntity wallet = walletRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("You are not logged in."));
                WalletSummary walletSummary = new WalletSummary(wallet.getWalletNumber(), wallet.getBalance(),
                                wallet.getCurrency().name(), wallet.getStatus().name());
                SecurityProfile securityProfile = new SecurityProfile(user.getTransactionPinHash() != null, true,
                                "Beginner",
                                true, "ACTIVE");
                return new AuthenticatedUserResponse(profile, walletSummary, securityProfile, user.getProfilePicUrl());
        }

        @Transactional
        public ChangeEmailResponse changeEmail(ChangeEmailRequest request) {

                UserEntity user = authService.getAuthenticatedUser();

                if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                        throw new IllegalArgumentException("Invalid password.");
                }

                if (userRepository.existsByEmail(request.newEmail())) {
                        throw new IllegalStateException("Email already in use.");
                }

                String oldEmail = user.getEmail();
                user.setEmail(request.newEmail());

                return new ChangeEmailResponse(
                                user.getId(),
                                oldEmail,
                                user.getEmail(),
                                user.getUpdatedAt(),
                                "Email updated successfully.");
        }

        @Transactional
        public ChangePhoneNumberResponse changePhoneNumber(ChangePhoneNumberRequest request) {

                UserEntity user = authService.getAuthenticatedUser();

                // Verify password
                if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                        throw new IllegalArgumentException("Invalid password.");
                }

                // Check uniqueness
                if (userRepository.existsByPhoneNumber(request.newPhoneNumber())) {
                        throw new IllegalStateException("Phone number already in use.");
                }

                String oldPhone = user.getPhoneNumber();
                user.setPhoneNumber(request.newPhoneNumber());

                return new ChangePhoneNumberResponse(
                                user.getId(),
                                oldPhone,
                                user.getPhoneNumber(),
                                user.getUpdatedAt(),
                                "Phone number updated successfully.");
        }

        @Transactional
        public List<RecentContactResponse> searchUsers(String query) {
                UserEntity currentUser = authService.getAuthenticatedUser();
                List<UserEntity> users = userRepository
                                .findByUserNameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                                query, query, query, PageRequest.of(0, 10));

                return users.stream()
                                .filter(user -> !user.getId().equals(currentUser.getId()))
                                .map(user -> new RecentContactResponse(
                                                user.getUserName(),
                                                LocalDateTime.now(), // Placeholder for search
                                                BigDecimal.ZERO, // Placeholder for search
                                                user.getProfilePicUrl()))
                                .collect(Collectors.toList());
        }

}