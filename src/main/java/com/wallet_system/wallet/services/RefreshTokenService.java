package com.wallet_system.wallet.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.wallet_system.wallet.entities.RefreshToken;
import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.exceptions.UnauthorizedException;
import com.wallet_system.wallet.models.response.TokenRequestResponse;
import com.wallet_system.wallet.repositories.RefreshTokenRepository;
import com.wallet_system.wallet.repositories.UserRepository;

@Service
public class RefreshTokenService {

    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public RefreshTokenService(AuthService authService, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TokenService tokenService) {
        this.authService = authService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public void deleteToken() {
        var user = authService.getAuthenticatedUser();
        refreshTokenRepository.deleteByUser(user);
    }

    public RefreshToken createToken(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // Delete existing refresh token for this user to avoid unique constraint violations
        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // Set expiration to 7 days
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    public TokenRequestResponse newToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        boolean isExpired = refreshToken.getExpiresAt().isBefore(Instant.now());
        if (isExpired) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token has expired");
        }
        String newToken = tokenService.generateTokenFromUser(refreshToken.getUser());
        return new TokenRequestResponse(newToken);
    }
}
