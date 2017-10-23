package com.db.awmd.challenge.performance;

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
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class ConcurrencyTest {

    @Autowired
    TransfersService transfersService;

    @Autowired
    AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void concurrencyByServiceTest() throws Exception {

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-234\",\"balance\":2000}"))
                .andExpect(status().isCreated());

        CyclicBarrier cb = new CyclicBarrier(400);

        ExecutorService executor = Executors.newFixedThreadPool(400);
        IntStream.range(0, 400)
                .forEach(i -> executor.submit(
                                () -> createTransferBarrier(cb,
                                        new Transfer("Id-123", "Id-234", BigDecimal.TEN)
                                )
                            )
                        );

        executor.awaitTermination(30, TimeUnit.SECONDS);

        Account accountFrom = accountsService.getAccount("Id-123");
        Account accountTo = accountsService.getAccount("Id-234");

        assertThat(accountFrom.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(accountTo.getBalance()).isEqualTo(BigDecimal.valueOf(6000));
    }

    public void createTransferBarrier (CyclicBarrier cb, Transfer transfer) {

        try {
            cb.await();
            transfersService.createTransfer(transfer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void concurrencyTransferTest() throws Exception {
        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-234\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-345\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-456\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-567\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-678\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-789\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-890\",\"balance\":5000}"))
                .andExpect(status().isCreated());

        ExecutorService executor = Executors.newFixedThreadPool(100);

        CyclicBarrier cb = new CyclicBarrier(100);

            IntStream.range(0, 100)
                    .forEach(i -> executor.submit(
                            () -> {
                                createTransferBarrier(cb,new Transfer("Id-123", "Id-234", BigDecimal.ONE));
                                createTransferBarrier(cb,new Transfer("Id-345", "Id-456", BigDecimal.ONE));
                                createTransferBarrier(cb,new Transfer("Id-567", "Id-678", BigDecimal.ONE));
                                createTransferBarrier(cb,new Transfer("Id-789", "Id-890", BigDecimal.ONE));
                            }
                            )
                    );

        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(accountsService.getAccount("Id-123").getBalance()).isEqualTo(BigDecimal.valueOf(4900));
        assertThat(accountsService.getAccount("Id-234").getBalance()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(accountsService.getAccount("Id-345").getBalance()).isEqualTo(BigDecimal.valueOf(4900));
        assertThat(accountsService.getAccount("Id-456").getBalance()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(accountsService.getAccount("Id-567").getBalance()).isEqualTo(BigDecimal.valueOf(4900));
        assertThat(accountsService.getAccount("Id-678").getBalance()).isEqualTo(BigDecimal.valueOf(5100));
        assertThat(accountsService.getAccount("Id-567").getBalance()).isEqualTo(BigDecimal.valueOf(4900));
        assertThat(accountsService.getAccount("Id-678").getBalance()).isEqualTo(BigDecimal.valueOf(5100));
    }

}
