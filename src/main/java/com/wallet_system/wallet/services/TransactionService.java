package com.wallet_system.wallet.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet_system.wallet.entities.LedgerEntity;
import com.wallet_system.wallet.entities.TransactionEntity;
import com.wallet_system.wallet.entities.UserEntity;
import com.wallet_system.wallet.entities.WalletEntity;
import com.wallet_system.wallet.enums.EntryType;
import com.wallet_system.wallet.enums.TransactionStatus;
import com.wallet_system.wallet.enums.TransactionType;
import com.wallet_system.wallet.exceptions.ResourceNotFoundException;
import com.wallet_system.wallet.models.request.FundWalletRequest;
import com.wallet_system.wallet.models.request.TransferRequest;
import com.wallet_system.wallet.models.response.FundWalletResponse;
import com.wallet_system.wallet.models.response.IdempotentCheckResponse;
import com.wallet_system.wallet.models.response.RecentContactResponse;
import com.wallet_system.wallet.models.response.TransactionsResponse;
import com.wallet_system.wallet.models.response.TransferResponse;
import com.wallet_system.wallet.repositories.LedgerRepository;
import com.wallet_system.wallet.repositories.TransactionRepository;
import com.wallet_system.wallet.repositories.UserRepository;
import com.wallet_system.wallet.repositories.WalletRepository;

@Service
public class TransactionService {
        private final TransactionRepository transactionRepository;
        private final WalletRepository walletRepository;
        private final AuthService authService;
        private final LedgerRepository ledgerRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository,
                        AuthService authService, LedgerRepository ledgerRepository, UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
                this.transactionRepository = transactionRepository;
                this.walletRepository = walletRepository;
                this.authService = authService;
                this.ledgerRepository = ledgerRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public FundWalletResponse fundWallet(FundWalletRequest request) {

                if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Amount must be greater than zero");
                }

                // Check idempotency
                TransactionEntity existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                                .orElse(null);
                if (existing != null) {
                        WalletEntity wallet = walletRepository.findByUser(existing.getUser())
                                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                        return new FundWalletResponse("Wallet already funded", existing.getReference(),
                                        existing.getAmount(),
                                        wallet.getBalance(), existing.getStatus().name());
                }

                // Get authenticated user
                UserEntity user = authService.getAuthenticatedUser();

                // Create Transaction
                TransactionEntity transaction = new TransactionEntity();
                transaction.setAmount(request.amount());
                transaction.setType(TransactionType.DEPOSIT);
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setIdempotencyKey(request.idempotencyKey());
                transaction.setReference(genereteReference());
                transaction.setUser(user);
                transaction.setSystemDescription(
                                user.getUserName() + " Fund their wallet with " + request.amount().toString());

                transactionRepository.save(transaction);

                // Get wallet
                WalletEntity wallet = walletRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

                var balanceBefore = wallet.getBalance();

                // Update balance
                BigDecimal newBalance = wallet.getBalance().add(request.amount());
                wallet.setBalance(newBalance);
                walletRepository.save(wallet);

                // Create ledger entry
                LedgerEntity ledger = new LedgerEntity();
                ledger.setTransaction(transaction);
                ledger.setAmount(request.amount());
                ledger.setWallet(wallet);
                ledger.setEntryType(EntryType.CREDIT);
                ledger.setCurrency(wallet.getCurrency());
                ledger.setBalanceBefore(balanceBefore);
                ledger.setBalanceAfter(newBalance);
                ledger.setReference(transaction.getReference());
                ledger.setUser(user);
                ledgerRepository.save(ledger);

                return new FundWalletResponse(
                                "Wallet funded successfully",
                                transaction.getReference(),
                                request.amount(),
                                newBalance,
                                transaction.getStatus().name());
        }

        @Transactional
        public TransferResponse transfer(TransferRequest request) {

                UserEntity user = authService.getAuthenticatedUser();

                if (!passwordEncoder.matches(request.transactionPin(), user.getTransactionPinHash())) {
                        throw new IllegalArgumentException("Invalid transaction pin");
                }

                // Amount can't be zero
                if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Amount must be greater than zero");
                }

                // Check idempotency
                TransactionEntity existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                                .orElse(null);
                if (existing != null && existing.getStatus() == TransactionStatus.SUCCESS) {
                        // WalletEntity wallet = walletRepository.findByUser(existing.getUser())
                        // .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
                        if (existing.getType() != TransactionType.TRANSFER) {
                                throw new IllegalStateException(
                                                "Idempotency key reuse: This key was previously used for a "
                                                                + existing.getType());
                        }
                        return mapToTransferResponse(existing, user);
                }

                // Find the recipient
                UserEntity recipient = userRepository.findByUserName(request.receiverUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

                if (user.getId().equals(recipient.getId())) {
                        throw new IllegalArgumentException("Cannot transfer to yourself");
                }

                WalletEntity senderWallet = walletRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("You are not logged in"));
                WalletEntity receiverWallet = walletRepository.findByUser(recipient)
                                .orElseThrow(() -> new ResourceNotFoundException("Reviever wallet not found"));

                BigDecimal senderWalletBalanceBefore = senderWallet.getBalance();
                BigDecimal receiverWalletBalanceBefore = receiverWallet.getBalance();

                if (senderWalletBalanceBefore.compareTo(request.amount()) < 0) {
                        throw new IllegalArgumentException("Insufficient Balance");
                }

                BigDecimal newSenderBalance = senderWalletBalanceBefore.subtract(request.amount());
                BigDecimal newReceiverBalance = receiverWalletBalanceBefore.add(request.amount());

                String senderNote = request.comment() != null ? request.comment() : "No note added";

                // Create the event
                TransactionEntity transaction = new TransactionEntity();
                transaction.setAmount(request.amount());
                transaction.setType(TransactionType.TRANSFER);
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setIdempotencyKey(request.idempotencyKey());
                transaction.setUser(user);
                transaction.setNote(request.comment());
                transaction.setReference(genereteReference());
                transaction.setSystemDescription(
                                user.getUserName() + " Sent " + request.amount().toString() + " to "
                                                + recipient.getUserName());
                transactionRepository.save(transaction);

                LedgerEntity senderEntry = new LedgerEntity();
                senderEntry.setTransaction(transaction);
                senderEntry.setBalanceBefore(senderWalletBalanceBefore);
                senderEntry.setBalanceAfter(newSenderBalance);
                senderEntry.setAmount(request.amount());
                senderEntry.setEntryType(EntryType.DEBIT);
                senderEntry.setUser(user);
                senderEntry.setDescription(
                                "Sent " + request.amount().toString() + " to " + recipient.getUserName() + ": "
                                                + senderNote);
                senderEntry.setWallet(senderWallet);
                senderEntry.setCurrency(senderWallet.getCurrency());
                senderEntry.setReference(transaction.getReference());
                ledgerRepository.save(senderEntry);

                LedgerEntity receiverEntry = new LedgerEntity();
                receiverEntry.setTransaction(transaction);
                receiverEntry.setBalanceBefore(receiverWalletBalanceBefore);
                receiverEntry.setBalanceAfter(newReceiverBalance);
                receiverEntry.setAmount(request.amount());
                receiverEntry.setEntryType(EntryType.CREDIT);
                receiverEntry.setUser(recipient);
                receiverEntry.setDescription(
                                "Received " + request.amount().toString() + " from " + user.getUserName() + ": "
                                                + senderNote);
                receiverEntry.setWallet(receiverWallet);
                receiverEntry.setCurrency(receiverWallet.getCurrency());
                receiverEntry.setReference(transaction.getReference());
                ledgerRepository.save(receiverEntry);

                senderWallet.setBalance(newSenderBalance);
                walletRepository.save(senderWallet);
                receiverWallet.setBalance(newReceiverBalance);
                walletRepository.save(receiverWallet);

                return new TransferResponse(recipient.getUserName(), transaction.getStatus(),
                                senderEntry.getEntryType(),
                                transaction.getAmount(), senderEntry.getDescription(), transaction.getReference());

        }

        @Transactional
        public List<RecentContactResponse> getRecentContacts() {
                UserEntity currentUser = authService.getAuthenticatedUser();
                List<TransactionEntity> transactions = transactionRepository
                                .findTop20ByUserAndTypeOrderByCreatedAtDesc(currentUser, TransactionType.TRANSFER);
                Map<UUID, RecentContactResponse> uniqueContacts = new LinkedHashMap<>();
                for (TransactionEntity transaction : transactions) {
                        for (LedgerEntity entry : transaction.getLedgerEntries()) {
                                UserEntity otherUser = entry.getUser();
                                if (otherUser.getId().equals(currentUser.getId())) {
                                        continue;
                                }
                                uniqueContacts.putIfAbsent(otherUser.getId(),
                                                new RecentContactResponse(otherUser.getUserName(),
                                                                entry.getTransaction().getCreatedAt(),
                                                                entry.getAmount(), otherUser.getProfilePicUrl()));
                        }
                        if (uniqueContacts.size() == 10) {
                                break;
                        }
                }
                return new ArrayList<>(uniqueContacts.values());
        }

        @Transactional
        public Page<TransactionsResponse> getTransactions(int page, int size) {

                UserEntity currentUser = authService.getAuthenticatedUser();
                WalletEntity currentUserWallet = walletRepository.findByUser(currentUser).orElse(null);

                if (currentUserWallet == null)
                        return Page.empty();

                Pageable pageable = PageRequest.of(page, size);

                Page<LedgerEntity> recent = ledgerRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);

                return recent.map(r -> new TransactionsResponse(
                                r.getTransaction().getSystemDescription(),
                                r.getAmount(),
                                r.getCreatedAt(),
                                r.getEntryType(),
                                r.getTransaction().getStatus(),
                                r.getReference(),
                                r.getDescription()));
        }

        public IdempotentCheckResponse checkIdempotency(String idempotencyKey) {
                TransactionEntity transaction = transactionRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(
                                () -> new ResourceNotFoundException(
                                                "Transaction with the idempotency key does not exists"));
                var response = new IdempotentCheckResponse("Transaction found", transaction.getReference(),
                                transaction.getAmount(), transaction.getStatus().name());
                return response;
        }

        private TransferResponse mapToTransferResponse(TransactionEntity transaction, UserEntity currentUser) {
                // 1. Get all ledger entries for this transaction
                List<LedgerEntity> entries = transaction.getLedgerEntries();

                // 2. Find the entry belonging to the logged-in user (The Sender)
                LedgerEntity userEntry = entries.stream()
                                .filter(e -> e.getUser().getId().equals(currentUser.getId()))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Your ledger entry not found"));

                // 3. Find the entry belonging to the other person (The Recipient)
                LedgerEntity recipientEntry = entries.stream()
                                .filter(e -> !e.getUser().getId().equals(currentUser.getId()))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Recipient ledger entry not found"));

                // 4. Build the Record
                return new TransferResponse(
                                recipientEntry.getUser().getUserName(), // Receiver's name
                                transaction.getStatus(), // TransactionStatus Enum
                                userEntry.getEntryType(), // EntryType Enum (likely DEBIT)
                                transaction.getAmount(), // The actual amount sent
                                "Transfer retrieved successfully", // Message
                                transaction.getIdempotencyKey() // Reference
                );
        }

        private String genereteReference() {
                return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
}
