package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.repository.TransfersRepositoryInMemory;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
  private TransfersRepositoryInMemory transfersRepositoryInMemory;

  @Autowired
  private WebApplicationContext webApplicationContext;

  Account accountOne = new Account("Id-123", BigDecimal.valueOf(3000));
  Account accountTwo = new Account("Id-234", BigDecimal.valueOf(2000));

  @Before
  public void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing transfers before each test.
    //this.transfersService.getTransfersRepository().clearTransfers();
    this.transfersRepositoryInMemory.clearTransfers();

    // Reset the existing accounts before each test.
    this.accountsService.getAccountsRepository().clearAccounts();

    // prior to create a transfer, we need to create the two accounts
    this.accountsService.createAccount(accountOne);
    this.accountsService.createAccount(accountTwo);
  }

  @Test
  public void createTransfer() throws Exception {

    String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}";
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.accountFromId", containsString("Id-123")))
            .andExpect(jsonPath("$.accountToId", containsString("Id-234")));

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(2600));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2400));
  }

  @Test
  public void createTransferNoAmount() throws Exception {

    String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\"}";
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().isBadRequest());

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(3000));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2000));
  }

  @Test
  public void createTransferNoBody() throws Exception {

    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(3000));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2000));
  }

  @Test
  public void createTransferNegativeAmount() throws Exception {

    String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":-300}";
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().isBadRequest());

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(3000));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2000));
  }

  @Test
  public void createTransferNegativeBalance() throws Exception {

    String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":5000}";
    MvcResult result = this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().is2xxSuccessful())
            .andReturn();

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(3000));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2000));

  }

  @Test
  public void createTransferEmptytransferId() throws Exception {

    String strJson = "{\"transferId\":" + "" + ",\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":100}";
    this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().isBadRequest());

    BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
    BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

    assertEquals(finalBalanceFrom, BigDecimal.valueOf(3000));
    assertEquals(finalBalanceTo, BigDecimal.valueOf(2000));
  }

  @Test
  public void getTransferById() throws Exception {

    String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}";

    Transfer transfer = new Transfer("Id-234", "Id-123", BigDecimal.TEN);
    this.transfersService.createTransfer(transfer);

    this.mockMvc.perform(get("/v1/transfers/" + transfer.getTransferId()))
      .andExpect(status().is2xxSuccessful())
      .andExpect(
        content().string("{\"present\":true}"));
  }

  @Test
  public void validateAmountIsSubtracted() throws Exception {

     String strJson = "{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-234\",\"amount\":400}";

     BigDecimal initialBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
     BigDecimal initialBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

     this.mockMvc.perform(post("/v1/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(strJson))
            .andExpect(status().is2xxSuccessful());

     BigDecimal finalBalanceFrom = this.accountsService.getAccount("Id-123").getBalance();
     BigDecimal finalBalanceTo = this.accountsService.getAccount("Id-234").getBalance();

     assertEquals(finalBalanceFrom, BigDecimal.valueOf(2600));
     assertEquals(finalBalanceTo, BigDecimal.valueOf(2400));

  }

}
