package com.kartik.paymentledger.dto;

import com.kartik.paymentledger.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String status;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.status = transaction.getStatus().name();
        this.amount = transaction.getAmount();
        this.fromAccountNumber = transaction.getFromAccount().getAccountNumber();
        this.toAccountNumber = transaction.getToAccount().getAccountNumber();
        this.createdAt = transaction.getCreatedAt();
    }

    // Getters only — response objects don't need setters
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getFromAccountNumber() { return fromAccountNumber; }
    public String getToAccountNumber() { return toAccountNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}