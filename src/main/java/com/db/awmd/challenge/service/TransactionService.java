package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.DuplicateTransactionIdException;
import com.db.awmd.challenge.repository.TransactionsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService {

    @Getter
    private final TransactionsRepository transactionsRepository;
    private final AccountsService accountsService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public TransactionService(TransactionsRepository transactionsRepository, AccountsService accountsService, EmailNotificationService emailNotificationService) {
        this.transactionsRepository = transactionsRepository;
        this.accountsService = accountsService;
        this.emailNotificationService = emailNotificationService;
    }

    public void createTransaction(Transaction transaction) throws DuplicateTransactionIdException {

        Transaction previousTransaction = transactionsRepository.getTransaction(transaction.getTransactionId());
        if (previousTransaction != null) {
            throw new DuplicateTransactionIdException(
                    "Transaction with id " + transaction.getTransactionId() + " is already completed.");
        }

        Account accountFrom = this.accountsService.getAccount(transaction.getAccountFromId());
        Account accountTo = this.accountsService.getAccount(transaction.getAccountToId());

        if (accountFrom.getBalance().compareTo(transaction.getAmount()) >= 0) {
            accountFrom.setBalance(accountFrom.getBalance().subtract(transaction.getAmount()));
            accountTo.setBalance(accountTo.getBalance().add(transaction.getAmount()));

            this.transactionsRepository.createTransaction(transaction);
            this.emailNotificationService.notifyAboutTransfer(accountTo, "Transfer identified as " + transaction.getTransactionId() + " is completed.");
        } else {
            log.info("Transaction " + transaction.getTransactionId() + "could not be created because source account had not enough funds.");
        }

    }

    public Transaction getTransaction(String transactionId) {
        return this.transactionsRepository.getTransaction(transactionId);
    }

}
