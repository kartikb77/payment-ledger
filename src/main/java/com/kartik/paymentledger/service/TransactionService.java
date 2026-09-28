package com.kartik.paymentledger.service;

import com.kartik.paymentledger.dto.*;
import com.kartik.paymentledger.entity.Account;
import com.kartik.paymentledger.entity.LedgerEntry;
import com.kartik.paymentledger.entity.Transaction;
import com.kartik.paymentledger.repository.AccountRepository;
import com.kartik.paymentledger.repository.LedgerEntryRepository;
import com.kartik.paymentledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final RestTemplate restTemplate;

    private static final String FRAUD_SERVICE_URL = "http://localhost:8000/score-transaction";

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.restTemplate = restTemplate;


    }
    private Account lockAccount(String accountNumber, String notFoundMessage) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new RuntimeException(notFoundMessage));
    }
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {

        // 1. Idempotency check
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return new TransactionResponse(existing.get());
        }



        // 2. Resolve accounts
        String from = request.getFromAccountNumber();
        String to = request.getToAccountNumber();
        boolean fromFirst = from.compareTo(to) <= 0;

        Account first = lockAccount(fromFirst ? from : to,
                fromFirst ? "From account not found" : "To account not found");
        Account second = lockAccount(fromFirst ? to : from,
                fromFirst ? "To account not found" : "From account not found");

        Account fromAccount = fromFirst ? first : second;
        Account toAccount = fromFirst ? second : first;

        // 3. Balance check
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // 4. Fraud check — call the Python microservice
        FraudScoreRequest fraudRequest = new FraudScoreRequest(
                request.getAmount().doubleValue(),
                LocalDateTime.now().getHour(),
                1, // transaction_velocity — hardcoded for now; a real system would query recent transaction count
                0, // is_new_device — hardcoded for now; a real system would check device fingerprint
                30 // account_age_days — hardcoded for now; a real system would compute from account creation date
        );

        FraudScoreResponse fraudResponse;
        try {
            fraudResponse = restTemplate.postForObject(FRAUD_SERVICE_URL, fraudRequest, FraudScoreResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Fraud check service unavailable");
        }

        Transaction.TransactionStatus status;
        if (fraudResponse == null) {
            status = Transaction.TransactionStatus.PENDING;
        } else if ("BLOCK".equals(fraudResponse.getDecision())) {
            status = Transaction.TransactionStatus.REJECTED;
        } else if ("REVIEW".equals(fraudResponse.getDecision())) {
            status = Transaction.TransactionStatus.FLAGGED;
        } else {
            status = Transaction.TransactionStatus.APPROVED;
        }

        // 5. Create the transaction record
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(status);
        transaction = transactionRepository.save(transaction);

        // If blocked, stop here — no ledger entries, no balance changes
        if (status == Transaction.TransactionStatus.REJECTED) {
            return new TransactionResponse(transaction);
        }

        // 6. Double-entry ledger: debit sender, credit receiver
        LedgerEntry debit = new LedgerEntry();
        debit.setTransaction(transaction);
        debit.setAccount(fromAccount);
        debit.setEntryType(LedgerEntry.EntryType.DEBIT);
        debit.setAmount(request.getAmount());
        ledgerEntryRepository.save(debit);

        LedgerEntry credit = new LedgerEntry();
        credit.setTransaction(transaction);
        credit.setAccount(toAccount);
        credit.setEntryType(LedgerEntry.EntryType.CREDIT);
        credit.setAmount(request.getAmount());
        ledgerEntryRepository.save(credit);

        // 7. Update account balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new TransactionResponse(transaction);
    }
}