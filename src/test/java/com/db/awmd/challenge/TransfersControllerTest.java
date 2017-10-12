package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransfersService;
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
public class TransfersControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private TransfersService transfersService;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  Account accountOne = new Account("Id-123", BigDecimal.valueOf(3000));
  Account accountTwo = new Account("Id-234", BigDecimal.valueOf(2000));

  @Before
  public void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing transfers before each test.
    transfersService.getTransfersRepository().clearTransfers();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();

    // prior to create a transfer, we need to create the two accounts
    accountsService.createAccount(accountOne);
    accountsService.createAccount(accountTwo);
  }

  @Test
  public void createTransfer() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"transfer-a\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}"))
            .andDo(print())
            .andExpect(status().isCreated());

    Transfer transfer = transfersService.getTransfer("transfer-a");
    assertThat(transfer.getAccountFromId()).isEqualTo("Id-123");
    assertThat(transfer.getAccountFromId()).isEqualTo("Id-123");
    assertThat(transfer.getAccountToId()).isEqualTo("Id-234");
    assertThat(transfer.getAmount()).isEqualTo(BigDecimal.valueOf(400));
  }

  @Test
  public void createDuplicateTransfer() throws Exception {
    // the same transfer cannot happen twice with the same transferId
    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
      .content("{\"transferId\":\"transfer-b\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
      .content("{\"transferId\":\"transfer-b\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNotTansferId() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":500}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNoAmount() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"transfer-c\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\"}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNegativeAmount() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"transfer-d\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":-300}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void createTransferNegativeBalance() throws Exception {
     this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"transfer-e\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":5000}"))
            .andExpect(status().is2xxSuccessful());

    Transfer transfer = transfersService.getTransfer("transfer-e");
    assertThat(transfer).isNull();

  }

  @Test
  public void createTransferEmptytransferId() throws Exception {
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":100}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void getTransfer() throws Exception {
    Transfer transfer = new Transfer("transfer-f","Id-234", "Id-123", BigDecimal.valueOf(1500));
    this.transfersService.createTransfer(transfer);
    this.mockMvc.perform(get("/v1/transfers/" + transfer.getTransferId()))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"transferId\":\"" + transfer.getTransferId() + "\",\"accountFromId\":\"Id-234\",\"accountToId\":\"Id-123\",\"amount\":1500}"));
  }

    @Test
  public void validateAmountIsSubtracted() throws Exception {

    Account accountFromBefore = accountsService.getAccount("Id-123");
    BigDecimal initialBalanceFrom = accountFromBefore.getBalance();

    Account accountToBefore = accountsService.getAccount("Id-234");
    BigDecimal initialBalanceTo = accountToBefore.getBalance();

    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"transferId\":\"transfer-a\",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}"))
            .andExpect(status().isCreated());

    Transfer transfer = transfersService.getTransfer("transfer-a");
    assertThat(transfer.getAccountFromId()).isEqualTo("Id-123");
    assertThat(transfer.getAccountToId()).isEqualTo("Id-234");
    assertThat(transfer.getAmount()).isEqualTo(BigDecimal.valueOf(400));

    // validate amount is subtracted from "from" account
    Account accountFromAfter = accountsService.getAccount(transfer.getAccountFromId());
    BigDecimal finalBalance = accountFromAfter.getBalance();
    assertThat(transfer.getAmount().add(finalBalance)).isEqualTo(initialBalanceFrom);

    // validate amount is added to "to" account
    Account accountToAfter = accountsService.getAccount(transfer.getAccountToId());
    BigDecimal finalBalanceTo = accountToAfter.getBalance();
    assertThat(initialBalanceTo.add(transfer.getAmount())).isEqualTo(finalBalanceTo);

  }

}
