package com.wallet_system.wallet.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.enums.Role;
import com.wallet_system.wallet.exceptions.ResourceNotFoundException;
import com.wallet_system.wallet.exceptions.UnauthorizedException;
import com.wallet_system.wallet.exceptions.UserAlreadyExistsException;
import com.wallet_system.wallet.models.request.CreatePinRequest;
import com.wallet_system.wallet.models.request.RegisterRequest;
import com.wallet_system.wallet.models.response.CreatePinResponse;
import com.wallet_system.wallet.models.response.CreateWalletResponse;
import com.wallet_system.wallet.models.response.RegisterResponse;
import com.wallet_system.wallet.models.response.RegisterWithWalletResponse;
import com.wallet_system.wallet.repositories.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    @Transactional
    public RegisterWithWalletResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exits");
        }
        if (userRepository.existsByUserName(request.userName())) {
            throw new UserAlreadyExistsException("Username already exits");
        }
        UserEntity user = new UserEntity();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(Role.USER);
        user.setUserName(request.userName());
        userRepository.save(user);

        RegisterResponse userRes = new RegisterResponse(user.getFirstName(), user.getLastName(), user.getUserName());
        CreateWalletResponse walletRes = walletService.create(user);
        return new RegisterWithWalletResponse(userRes, walletRes);
    }

    public CreatePinResponse createPin(CreatePinRequest request) {
        var user = getAuthenticatedUser();
        if(user.getTransactionPinHash() != null) {
            throw new UserAlreadyExistsException("Pin already exits, please reset your pin");
        }
        user.setTransactionPinHash(passwordEncoder.encode(request.pin()));
        userRepository.save(user);
        return new CreatePinResponse("Pin created successfully");
    }

    public UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        } 
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No Logged in user"));
    }
}
