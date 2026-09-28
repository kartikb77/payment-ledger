# Payment Ledger Service

A payments backend demonstrating double-entry ledger accounting, idempotent transaction processing, JWT authentication, and real-time fraud scoring via an integrated Python microservice.

## Architecture

- **Java 17 + Spring Boot 4** — REST API, transaction orchestration, double-entry ledger
- **MySQL** — persistent storage with ACID guarantees
- **Spring Security + JWT** — stateless authentication
- **Python + FastAPI** — separate fraud-scoring microservice (Random Forest model)

Spring Boot calls the fraud-scoring service synchronously via REST for each transaction, then approves, flags, or rejects based on the returned risk score.

## Key design decisions

- **Double-entry ledger**: every transaction writes two `LedgerEntry` rows (a debit and a credit) rather than mutating a single balance field, so balances are always derivable and auditable from the ledger itself.
- **Idempotency**: each transaction request carries a client-supplied idempotency key; duplicate requests return the original result instead of reprocessing.
- **`@Transactional` boundaries**: ledger writes and balance updates are wrapped in a single database transaction, so a partial failure never leaves a debit without its matching credit.

## Setup

### Java service
1. Create a MySQL database named `paymentledger`
2. Update `src/main/resources/application.properties` with your DB credentials
3. `./mvnw spring-boot:run`

### Python fraud service
1. `cd fraud-scoring-service && source venv/bin/activate`
2. `python train_model.py` (trains and saves the model)
3. `uvicorn app.main:app --reload --port 8000`

## API overview

- `POST /api/auth/register`, `POST /api/auth/login` — auth
- `POST /api/accounts` — create account
- `POST /api/transactions` — process a payment (requires JWT)

## Known limitations / next steps

- Row-level locking (`@Lock(PESSIMISTIC_WRITE)`) not yet implemented — balance updates are not protected against concurrent writes to the same account under high load
- Fraud-check features (`transaction_velocity`, `is_new_device`, `account_age_days`) are currently simplified placeholders rather than computed from real transaction history
- Test coverage currently limited to core validation logic in `TransactionService`; fraud-integration path would benefit from a mocked `RestTemplate` test