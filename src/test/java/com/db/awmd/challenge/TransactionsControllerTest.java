package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransactionsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  Account accountOne = new Account("Id-123", BigDecimal.valueOf(3000));
  Account accountTwo = new Account("Id-234", BigDecimal.valueOf(2000));

  @Before
  public void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing transactions before each test.
    transactionService.getTransactionsRepository().clearTransactions();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();

    // prior to create a transaction, we need to create the two accounts
    accountsService.createAccount(accountOne);
    accountsService.createAccount(accountTwo);
  }

  @Test
  public void createTransaction() throws Exception {

    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transactionId\":\"transaction-a\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}"))
            .andDo(print())
            .andExpect(status().isCreated());

    Transaction transaction = transactionService.getTransaction("transaction-a");
    assertThat(transaction.getAccountFromId()).isEqualTo("Id-123");
    assertThat(transaction.getAccountFromId()).isEqualTo("Id-123");
    assertThat(transaction.getAccountToId()).isEqualTo("Id-234");
    assertThat(transaction.getAmount()).isEqualTo(400);
  }

  @Test
  public void createDuplicateTransaction() throws Exception {
    // the same transaction cannot happen twice with the same transactionId

    this.mockMvc.perform(post("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
      .content("{\"transactionId\":\"transaction-b\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
      .content("{\"transactionId\":\"transaction-b\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionNoTransactionId() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":500}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionNoAmount() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transactionId\":\"transaction-c\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\"}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionNegativeAmount() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transactionId\":\"transaction-d\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":-300}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transactionId\":\"transaction-e\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":5000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransactionEmptyTransactionId() throws Exception {
    this.mockMvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transactionId\":\"\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":100}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void getTransaction() throws Exception {
    String uniqueTransactionId = "transaction-" + System.currentTimeMillis();
    Transaction transaction = new Transaction(uniqueTransactionId, "Id-678", "Id-789", BigDecimal.valueOf(1500));
    this.transactionService.createTransaction(transaction);
    this.mockMvc.perform(get("/v1/transactions/" + uniqueTransactionId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"transactionId\":\"" + uniqueTransactionId + "\",\"accountFromId\":\"Id-678\",\"accountToId\":\"Id-789\",\"amount\":1500}"));
  }

  //  @Test
//  public void validateAmountIsSubstracted() throws Exception {
//
//    this.mockMvc.perform(post("/v1/transactions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content("{\"transactionId\":\"transaction-a\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}"))
//            .andExpect(status().isCreated());
//
//    Transaction transaction = transactionService.getTransaction("transaction-a");
//    assertThat(transaction.getAccountFromId()).isEqualTo("Id-123");
//    assertThat(transaction.getAccountFromId()).isEqualTo("Id-123");
//    assertThat(transaction.getAccountToId()).isEqualTo("Id-234");
//    assertThat(transaction.getAmount()).isEqualTo(400);
//  }




}
