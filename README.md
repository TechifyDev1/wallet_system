# Wallet System

A Spring Boot backend for the wallet platform. This service powers the companion Flutter mobile app in `wallet_mobile` and handles authentication, wallet creation, wallet funding, transfers, transaction history, and user account management.

## Overview

`wallet_system` is a Java 21 backend built with Spring Boot, Spring Security, Spring Data JPA, and MySQL. It exposes REST endpoints consumed by the mobile app and uses JWT-based authentication for protected routes, with refresh-token support for app clients.

## Full-Stack Context

This backend works together with the Flutter mobile client repository:

[wallet_mobile](https://github.com/TechifyDev1/wallet_mobile)

Across both projects, I implemented:

- Mobile wallet flows in Flutter with Riverpod and Cupertino UI
- REST API endpoints in Spring Boot
- Authentication, refresh-token handling, and protected resource access
- User registration with automatic wallet creation
- Wallet funding, transfers, ledger entries, and transaction history
- Profile retrieval, user search, recent contacts, and account updates

## What The Backend Does

- Register users and create a wallet for each new account
- Authenticate users with email and password
- Issue JWT access tokens
- Issue refresh tokens for app clients
- Allow first-time transaction PIN creation
- Reset forgotten passwords with email and secret key
- Reset passwords for authenticated users
- Return the authenticated user's profile, wallet summary, and security info
- Change email and phone number after password verification
- Search users for transfer recipients
- Return recent transfer contacts
- Fund a wallet with idempotency protection
- Transfer money between users with PIN verification
- Record ledger entries for debit and credit sides of transfers
- Return paginated transaction history
- Check an idempotency key for an existing transaction
- Delete refresh tokens on logout

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
      .env
  test/
    java/com/wallet_system/wallet/
```

## Main Modules

- `controllers/`
  Exposes authentication, user, and transaction endpoints.
- `services/`
  Contains the business logic for auth, tokens, wallet creation, profile operations, funding, and transfers.
- `entities/`
  Maps users, wallets, transactions, ledgers, and refresh tokens to the database.
- `repositories/`
  Handles persistence through Spring Data JPA.
- `models/request` and `models/response`
  Define the API contracts used by the mobile app.

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

## Authentication Flow

1. A user logs in with email and password.
2. The backend authenticates with Spring Security.
3. An access token is generated.
4. If the request includes `X-Client-Type: app`, the backend also creates a refresh token payload for the mobile app.
5. Protected routes require a valid bearer token.
6. The mobile app uses the refresh token flow when access tokens expire.

## Wallet And Transaction Behavior

- New users receive a wallet during registration.
- New wallets are created with `NGN` currency and a zero balance.
- Funding uses an idempotency key to avoid duplicate deposits.
- Transfers require a valid transaction PIN.
- Transfers create both debit and credit ledger entries.
- Transaction history is derived from ledger records and returned in descending order.

## Configuration

The app reads database settings from `src/main/resources/.env` through `application.properties`.

Required environment keys:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

`application.properties` currently expects:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

## Getting Started

### Prerequisites

- Java 21
- A MySQL server
- Gradle wrapper support enabled in your environment

### Configure The Database

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

If your backend runs on a different host or port, update the mobile app base URL in `wallet_mobile/lib/src/core/network/api_endpoints.dart`.

## Testing

This project currently includes a basic Spring context-load test.

```bash
./gradlew test
```

## Notes

- Protected routes are configured in `SecurityConfig` using stateless JWT authentication.
- The backend is set up for app clients and also contains cookie-based login handling for non-app clients.
- Error responses are centralized through a global exception handler.
