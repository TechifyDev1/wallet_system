package com.wallet_system.wallet.controllers;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet_system.wallet.models.request.CreatePinRequest;
import com.wallet_system.wallet.models.request.ForgotPasswordRequest;
import com.wallet_system.wallet.models.request.LoginRequest;
import com.wallet_system.wallet.models.request.RegisterRequest;
import com.wallet_system.wallet.models.request.ResetPasswordRequest;
import com.wallet_system.wallet.models.request.TokenRefreshRequest;
import com.wallet_system.wallet.models.response.CreatePinResponse;
import com.wallet_system.wallet.models.response.ForgotPasswordResponse;
import com.wallet_system.wallet.models.response.LoginResponse;
import com.wallet_system.wallet.models.response.RegisterWithWalletResponse;
import com.wallet_system.wallet.models.response.ResetPasswordResponse;
import com.wallet_system.wallet.models.response.TokenRequestResponse;
import com.wallet_system.wallet.services.AuthService;
import com.wallet_system.wallet.services.RefreshTokenService;
import com.wallet_system.wallet.services.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
            TokenService tokenService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterWithWalletResponse> register(@RequestBody @Valid RegisterRequest request) {
        RegisterWithWalletResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Validated LoginRequest request,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        String token = tokenService.generateToken(authentication);

        if ("app".equalsIgnoreCase(clientType)) {
            var refreshTokenEntity = refreshTokenService.createToken(authentication);
            var refreshTokenResponse = new com.wallet_system.wallet.models.response.RefreshTokenResponse(
                refreshTokenEntity.getToken(), 
                refreshTokenEntity.getExpiresAt()
            );
            return ResponseEntity.ok(
                    new LoginResponse("Login successful", token, refreshTokenResponse));
        }

        ResponseCookie cookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(false) // change in production
                .path("/")
                .maxAge(900)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString(), HttpHeaders.SET_COOKIE, "refresh-token=" + refreshTokenService.createToken(authentication).getToken() + "; HttpOnly; Path=/; Max-Age=" + 7 * 24 * 60 * 60 + "; SameSite=Strict")
                .body(new LoginResponse("Login successful", null, null));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<CreatePinResponse> setPin(@RequestBody @Valid CreatePinRequest request) {
        var response = authService.createPin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        var response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        var response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenRequestResponse> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        TokenRequestResponse newToken = refreshTokenService.newToken(request.refreshToken());
        return ResponseEntity.ok(newToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        refreshTokenService.deleteToken();
        return ResponseEntity.ok("Logged out successfully");
    }
}
