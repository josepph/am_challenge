package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.DuplicateTransactionIdException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountsService accountsService;

    @Autowired
    public TransactionController(
            TransactionService transactionService,
            AccountsService accountsService) {
        this.transactionService = transactionService;
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTransaction(@RequestBody Transaction transaction) {

        try {
            this.transactionService.createTransaction(transaction);
        } catch (DuplicateTransactionIdException dtie) {
            return new ResponseEntity<>(dtie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{transactionId}")
    public Transaction getTransaction(@PathVariable String transactionId) {
        log.info("Retrieving transaction for id {}", transactionId);
        return this.transactionService.getTransaction(transactionId);
    }

}
