package com.wallet_system.wallet.controllers;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet_system.wallet.models.request.CreatePinRequest;
import com.wallet_system.wallet.models.request.LoginRequest;
import com.wallet_system.wallet.models.request.RegisterRequest;
import com.wallet_system.wallet.models.response.CreatePinResponse;
import com.wallet_system.wallet.models.response.LoginResponse;
import com.wallet_system.wallet.models.response.RegisterWithWalletResponse;
import com.wallet_system.wallet.services.AuthService;
import com.wallet_system.wallet.services.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
            TokenService tokenService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
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
            return ResponseEntity.ok(
                    new LoginResponse("Login successful", token));
        }

        ResponseCookie cookie = ResponseCookie.from("auth-token", token)
                .httpOnly(true)
                .secure(false) // change in production
                .path("/")
                .maxAge(900)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse("Login successful", null));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<CreatePinResponse> setPin(@RequestBody @Valid CreatePinRequest request) {
        var response = authService.createPin(request);
        return ResponseEntity.ok(response);
    }
}
