package com.kartik.paymentledger;

import com.kartik.paymentledger.dto.TransactionRequest;
import com.kartik.paymentledger.entity.Account;
import com.kartik.paymentledger.repository.AccountRepository;
import com.kartik.paymentledger.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        Account acc1 = new Account();
        acc1.setAccountNumber("TEST001");
        acc1.setOwnerName("Alice");
        acc1.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(acc1);

        Account acc2 = new Account();
        acc2.setAccountNumber("TEST002");
        acc2.setOwnerName("Bob");
        acc2.setBalance(new BigDecimal("500.00"));
        accountRepository.save(acc2);
    }

    @Test
    void insufficientFundsThrowsException() {
        TransactionRequest request = new TransactionRequest();
        request.setIdempotencyKey("test-insufficient-1");
        request.setFromAccountNumber("TEST001");
        request.setToAccountNumber("TEST002");
        request.setAmount(new BigDecimal("999999.00"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTransaction(request);
        });

        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    void fromAccountNotFoundThrowsException() {
        TransactionRequest request = new TransactionRequest();
        request.setIdempotencyKey("test-notfound-1");
        request.setFromAccountNumber("NONEXISTENT");
        request.setToAccountNumber("TEST002");
        request.setAmount(new BigDecimal("50.00"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTransaction(request);
        });

        assertEquals("From account not found", exception.getMessage());
    }
}