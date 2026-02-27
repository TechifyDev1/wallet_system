package com.wallet_system.wallet.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wallet_system.wallet.models.request.FundWalletRequest;
import com.wallet_system.wallet.models.request.TransferRequest;
import com.wallet_system.wallet.models.response.FundWalletResponse;
import com.wallet_system.wallet.models.response.IdempotentCheckResponse;
import com.wallet_system.wallet.models.response.TransactionsResponse;
import com.wallet_system.wallet.models.response.TransferResponse;
import com.wallet_system.wallet.services.TransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
public class TransactionsController {

    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/fund")
    public ResponseEntity<FundWalletResponse> fund(@Valid @RequestBody FundWalletRequest request) {
        var response = transactionService.fundWallet(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/check/{idempotencyKey}")
    public ResponseEntity<IdempotentCheckResponse> check(
            @PathVariable @NotBlank(message = "IdempotencyKey must be provided") String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.checkIdempotency(idempotencyKey));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> send(@RequestBody @Valid TransferRequest request) {
        var response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionsResponse>> getRecent(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTransactions(page, size));
    }
}
