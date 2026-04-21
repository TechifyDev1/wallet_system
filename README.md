# Wallet System

A Spring Boot backend for the wallet platform. This service powers the companion Flutter app in `wallet_mobile` and handles authentication, wallet creation, funding, transfers, transaction history, and account-related profile updates.

## Full-Stack Context

This backend works together with the Flutter mobile client:

[wallet_mobile](https://github.com/TechifyDev1/wallet_mobile)

Across both repositories, the platform provides:

- Mobile wallet flows in Flutter
- REST APIs in Spring Boot
- JWT-secured authenticated routes
- Refresh-token support for app clients
- Automatic wallet creation during onboarding
- Wallet funding, transfers, ledger tracking, and transaction history
- Profile retrieval and account update operations

## What The Backend Does

- Register new users
- Create a wallet during registration
- Authenticate users with email and password
- Issue JWT access tokens
- Issue refresh tokens for mobile clients
- Allow first-time transaction PIN creation
- Support forgot-password recovery with email and secret key
- Support password reset for authenticated users
- Return authenticated user profile, wallet summary, and security info
- Change email and phone number after password verification
- Search users for transfer recipients
- Return recent transfer contacts
- Fund wallets
- Transfer money between users
- Return paginated transaction history
- Check transactions by idempotency key
- Delete refresh tokens during logout

## Backend Responsibilities For The Mobile App

The Flutter app handles session storage and user interactions, while this backend is responsible for enforcing the rules that protect account access and money movement.

### Authentication And Access Control

- `Spring Security`
  Protects non-public routes and keeps wallet/profile endpoints behind authentication.
- `JWT access tokens`
  Used by the mobile app for authenticated requests.
- `Refresh tokens`
  Issued for app clients so short-lived access tokens can be renewed without repeated login.
- `Password hashing`
  Passwords and transaction PINs are verified through `PasswordEncoder` instead of plain-text comparison.

### Transaction Integrity

- `@Transactional service methods`
  Funding and transfer flows run inside database transactions.
- `Pessimistic wallet locking`
  Wallet lookup uses pessimistic write locking to reduce concurrent balance-update conflicts.
- `Idempotency protection`
  Funding and transfer requests use idempotency keys to prevent duplicate processing on retries.
- `Ledger recording`
  Transfer activity creates debit and credit ledger entries for a clearer transaction trail.

### Account Protection

- `Password re-verification`
  Sensitive profile changes require the current password.
- `First-time transaction PIN setup`
  Transfer security includes a dedicated PIN created after onboarding.
- `Forgot-password recovery with secret key`
  Recovery requires both email and the stored recovery secret.

## Main Modules

- `config/`
  Security, environment, and application configuration.
- `controllers/`
  Auth, user, and transaction endpoints.
- `services/`
  Business logic for auth, tokens, wallets, users, funding, and transfers.
- `entities/`
  JPA models for users, wallets, transactions, ledgers, and refresh tokens.
- `repositories/`
  Data access with Spring Data JPA.
- `models/request` and `models/response`
  Contracts used by the mobile app.

## Implemented API Routes

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/set-pin`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/refresh`
- `POST /api/auth/logout`

### User

- `GET /api/user/me`
- `GET /api/user/me/recent-contact`
- `POST /api/user/email/change`
- `POST /api/user/phone/change`
- `GET /api/user/search?query=...`

### Transactions

- `POST /api/fund`
- `POST /api/transfer`
- `GET /api/transactions?page=0&size=10`
- `GET /api/check/{idempotencyKey}`

## Wallet And Transaction Behavior

- Registration creates both the user and the wallet.
- Wallets start with `NGN` currency and zero balance.
- Funding uses an idempotency key so repeated requests do not create duplicate deposits.
- Transfers require a valid transaction PIN.
- Transfer processing records both sender and receiver ledger entries.
- Transaction history is returned from ledger records in descending order.

## Important Files

- `src/main/java/com/wallet_system/wallet/config/SecurityConfig.java`
  Stateless security setup, route protection, JWT encoder/decoder, and password encoder configuration.
- `src/main/java/com/wallet_system/wallet/controllers/AuthController.java`
  Registration, login, PIN setup, refresh, forgot-password, reset-password, and logout endpoints.
- `src/main/java/com/wallet_system/wallet/controllers/UserController.java`
  Authenticated profile, recent contacts, user search, and sensitive profile update endpoints.
- `src/main/java/com/wallet_system/wallet/controllers/TransactionsController.java`
  Funding, transfer, idempotency check, and transaction history endpoints.
- `src/main/java/com/wallet_system/wallet/services/TransactionService.java`
  Funding and transfer business rules, ledger writes, and transaction retrieval.
- `src/main/java/com/wallet_system/wallet/repositories/WalletRepository.java`
  Pessimistic locking support for balance-changing operations.
- `src/main/java/com/wallet_system/wallet/entities/TransactionEntity.java`
  Transaction persistence including unique idempotency key support.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring OAuth2 Resource Server
- Spring Data JPA
- MySQL
- Gradle
- Jakarta Validation
- `spring-dotenv` / `java-dotenv`

## Project Structure

```text
src/
  main/
    java/com/wallet_system/wallet/
      config/
      controllers/
      entities/
      enums/
      exceptions/
      models/
        request/
        response/
      repositories/
      services/
    resources/
      application.properties
  test/
    java/com/wallet_system/wallet/
```

## Authentication Flow

1. A user logs in with email and password.
2. Spring Security authenticates the request.
3. The backend issues an access token.
4. App clients also receive a refresh token.
5. Protected routes require a valid bearer token.
6. The mobile app uses the refresh flow when the access token expires.

## Configuration

Database settings are loaded from environment variables through `application.properties`.

Required keys:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Current datasource configuration:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

## Getting Started

### Prerequisites

- Java 21
- MySQL
- Gradle wrapper support enabled in your environment

### Configure Environment

Create or update `src/main/resources/.env` with your local database values:

```env
DB_URL=jdbc:mysql://localhost:3306/walletdb
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### Run The Backend

```bash
./gradlew bootRun
```

The mobile app currently points to:

```text
http://192.168.0.164:8080/api
```

If your backend runs on another host or port, update the base URL in `wallet_mobile/lib/src/core/network/api_endpoints.dart`.

## Testing

```bash
./gradlew test
```

## Notes

- Error responses are centralized in the global exception handler.
- The backend supports mobile app clients and also contains cookie-oriented login handling for non-app clients.
- Security and transaction guarantees described in the mobile README are enforced here on the server side.
