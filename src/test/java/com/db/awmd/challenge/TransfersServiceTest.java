package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.repository.TransfersRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.TransfersService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersServiceTest {

  @Autowired
  private TransfersService transfersService;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private TransfersRepositoryInMemory transfersRepositoryInMemory;

  @MockBean
  private EmailNotificationService emailNotificationService;

  @Before
  public void prepareMockMvc() throws Exception {
    // Reset the existing accounts and transfers before each test.
    this.accountsService.getAccountsRepository().clearAccounts();
    this.transfersRepositoryInMemory.clearTransfers();
   }

  @Test
  public void addTransfer() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    Transfer transfer = new Transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.TEN);
    this.transfersService.createTransfer(transfer);

    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getTransferId()).isEqualTo(transfer.getTransferId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAccountFromId()).isEqualTo(transfer.getAccountFromId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAccountToId()).isEqualTo(transfer.getAccountToId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAmount()).isEqualTo(transfer.getAmount());
  }

  @Test
  public void testNotification() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    Transfer transfer = new Transfer(accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transfersService.createTransfer(transfer);

    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getTransferId()).isEqualTo(transfer.getTransferId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAccountFromId()).isEqualTo(transfer.getAccountFromId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAccountToId()).isEqualTo(transfer.getAccountToId());
    assertThat(this.transfersService.getTransfer(transfer.getTransferId()).get().getAmount()).isEqualTo(transfer.getAmount());
    verify(this.emailNotificationService, atLeastOnce()).notifyAboutTransfer(eq(accountTo), anyString());
  }
}
