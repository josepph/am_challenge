package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transaction;

public interface TransactionsRepository {

    Transaction getTransaction(String transactionId);

    void createTransaction(Transaction transaction);

    void clearTransactions();

}
