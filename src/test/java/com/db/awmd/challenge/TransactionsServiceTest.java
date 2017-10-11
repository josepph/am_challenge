package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.DuplicateTransactionIdException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionsServiceTest {

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addTransaction() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    Transaction transaction = new Transaction("transaction-one", accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transactionService.createTransaction(transaction);

    assertThat(this.transactionService.getTransaction("transaction-one")).isEqualTo(transaction);
  }

  @Test
  public void addTransaction_failsOnDuplicateTransactionId() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    String uniqueId = "Transaction-" + System.currentTimeMillis();
    Transaction transaction = new Transaction(uniqueId, accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transactionService.createTransaction(transaction);

    try {
      this.transactionService.createTransaction(transaction);
      fail("Should have failed when creating a new Transaction with same transactionId");
    } catch (DuplicateTransactionIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Transaction id " + uniqueId + " is already completed.");
    }

  }
}
