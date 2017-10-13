package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateTransferIdException;
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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersServiceTest {

  @Autowired
  private TransfersService transfersService;

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private EmailNotificationService emailNotificationService;

  @Before
  public void prepareMockMvc() throws Exception {
    // Reset the existing accounts before each test.
    this.accountsService.getAccountsRepository().clearAccounts();
   }

  @Test
  public void addTransfer() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    Transfer transfer = new Transfer("transfer-one", accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transfersService.createTransfer(transfer);

    assertThat(this.transfersService.getTransfer(transfer.getTransferId())).isEqualTo(transfer);
  }

  @Test
  public void addTransfer_failsOnDuplicateTransferId() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    String uniqueId = "Transfer-" + System.currentTimeMillis();
    Transfer transfer = new Transfer(uniqueId, accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transfersService.createTransfer(transfer);

    try {
      this.transfersService.createTransfer(transfer);
      fail("Should have failed when creating a new transfer with same transferId");
    } catch (DuplicateTransferIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Transfer with id " + transfer.getTransferId() + " is already completed.");
    }
  }

  @Test
  public void testNotification() throws Exception {
    Account accountFrom = new Account("Id-123");
    accountFrom.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(accountFrom);

    Account accountTo = new Account("Id-234");
    accountTo.setBalance(new BigDecimal(2000));
    this.accountsService.createAccount(accountTo);

    Transfer transfer = new Transfer("transfer-one", accountFrom.getAccountId(), accountTo.getAccountId(), BigDecimal.valueOf(500));
    this.transfersService.createTransfer(transfer);

    assertThat(this.transfersService.getTransfer(transfer.getTransferId())).isEqualTo(transfer);

    verify(this.emailNotificationService, atLeastOnce()).notifyAboutTransfer(eq(accountTo), anyString());
  }

}
