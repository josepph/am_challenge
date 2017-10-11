package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.DuplicateTransactionIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class TransactionsRepositoryInMemory implements TransactionsRepository {

    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    @Override
    public Transaction getTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    @Override
    public void createTransaction(Transaction transaction) throws DuplicateTransactionIdException {
        if ((transaction.getAmount()).compareTo(BigDecimal.ZERO) > 0) {
            log.info("Cannot create transaction because amount has to be a positive value.");
        } else {
            transactions.put(transaction.getTransactionId(), transaction);
        }
    }

    @Override
    public void clearTransactions() {
        transactions.clear();
    }
}
