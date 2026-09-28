package com.kartik.paymentledger.controller;

import com.kartik.paymentledger.dto.TransactionRequest;
import com.kartik.paymentledger.dto.TransactionResponse;
import com.kartik.paymentledger.service.TransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        return transactionService.processTransaction(request);
    }
}