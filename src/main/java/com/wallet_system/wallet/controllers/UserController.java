package com.wallet_system.wallet.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet_system.wallet.models.request.ChangeEmailRequest;
import com.wallet_system.wallet.models.request.ChangePhoneNumberRequest;
import com.wallet_system.wallet.models.response.AuthenticatedUserResponse;
import com.wallet_system.wallet.models.response.ChangeEmailResponse;
import com.wallet_system.wallet.models.response.ChangePhoneNumberResponse;
import com.wallet_system.wallet.models.response.RecentContactResponse;
import com.wallet_system.wallet.services.TransactionService;
import com.wallet_system.wallet.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final TransactionService transactionService;

    public UserController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> getAuthenticatedUser() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAuthenticatedUser());
    }

    @GetMapping("/me/recent-contact")
    public ResponseEntity<List<RecentContactResponse>> getRecentContacts() {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getRecentContacts());
    }

    @PostMapping("/email/change")
    public ResponseEntity<ChangeEmailResponse> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request) {

        return ResponseEntity.ok(userService.changeEmail(request));
    }

    @PostMapping("/phone/change")
    public ResponseEntity<ChangePhoneNumberResponse> changePhoneNumber(
            @Valid @RequestBody ChangePhoneNumberRequest request) {

        return ResponseEntity.ok(userService.changePhoneNumber(request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecentContactResponse>> searchUsers(
            @org.springframework.web.bind.annotation.RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }
}
