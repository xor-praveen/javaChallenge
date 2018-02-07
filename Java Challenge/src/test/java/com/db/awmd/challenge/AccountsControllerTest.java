package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

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

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
		this.accountsService.clearAccounts();
	}

	@Test
	public void transferMoney() throws Exception {
		String uniqueAccountIdFrom = "Id-F" + System.currentTimeMillis();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("1410.45"));
		this.accountsService.createAccount(accountFrom);
		String uniqueAccountIdTo = "Id-T" + System.currentTimeMillis();
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("1210.45"));
		this.accountsService.createAccount(accountTo);
		this.mockMvc.perform(get("/v1/accounts/transfer/" + uniqueAccountIdFrom + "/" + uniqueAccountIdTo + "/" + new BigDecimal("500.45"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void transferMoneyInsufficiantFunds() throws Exception {
		String uniqueAccountIdFrom = "Id-F1" + System.currentTimeMillis();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("500.45"));
		this.accountsService.createAccount(accountFrom);
		String uniqueAccountIdTo = "Id-T1" + System.currentTimeMillis();
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("1220.45"));
		this.accountsService.createAccount(accountTo);
		this.mockMvc.perform(get("/v1/accounts/transfer/" + uniqueAccountIdFrom + "/" + uniqueAccountIdTo + "/" + new BigDecimal("600.45"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void transferMoneyMinimumBalance() throws Exception {
		String uniqueAccountIdFrom = "Id-F1" + System.currentTimeMillis();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("0.0"));
		this.accountsService.createAccount(accountFrom);
		String uniqueAccountIdTo = "Id-T1" + System.currentTimeMillis();
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("1220.45"));
		this.accountsService.createAccount(accountTo);
		this.mockMvc.perform(get("/v1/accounts/transfer/" + uniqueAccountIdFrom + "/" + uniqueAccountIdTo + "/" + new BigDecimal("600.45"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void transferMoneyZeroAmount() throws Exception {
		String uniqueAccountIdFrom = "Id-FMB" + System.currentTimeMillis();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("700.45"));
		this.accountsService.createAccount(accountFrom);
		String uniqueAccountIdTo = "Id-TMB" + System.currentTimeMillis();
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("1220.45"));
		this.accountsService.createAccount(accountTo);
		this.mockMvc.perform(get("/v1/accounts/transfer/" + uniqueAccountIdFrom + "/" + uniqueAccountIdTo + "/" + new BigDecimal("0.0"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}

	@Test
	public void transferMoneyInvalidAccount() throws Exception {
		String uniqueAccountIdFrom = "Id-FMB" + System.currentTimeMillis();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("700.45"));
		this.accountsService.createAccount(accountFrom);
		String uniqueAccountIdTo = "Id-TMB" + System.currentTimeMillis();
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("1220.45"));
		this.accountsService.createAccount(accountTo);
		this.mockMvc.perform(get("/v1/accounts/transfer/" + 0 + "/" + uniqueAccountIdTo + "/" + new BigDecimal("0.0"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void deposit() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("2200.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/deposit/" + uniqueAccountId + "/" + new BigDecimal("200.45"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void depositInvalidAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("2200.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/deposit/" + 0 + "/" + new BigDecimal("200.45"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void depositInvalidAmont() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("2200.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/deposit/" + uniqueAccountId + "/" + new BigDecimal("0.0"))).andExpect(status().isOk());
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void withdraw() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("1000.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/withdraw/" + uniqueAccountId + "/amount/" + new BigDecimal("100.45")));
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void withdrawInsufficiantFunds() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("400.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/withdraw/" + uniqueAccountId + "/amount/" + new BigDecimal("2000.45")));
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void withdrawInvalidAmount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("1000.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/withdraw/" + uniqueAccountId + "/amount/" + new BigDecimal("0.0")));
		this.accountsService.clearAccounts();
	}
	
	@Test
	public void withdrawInvalidAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("1000.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/withdraw/" + 0 + "/amount/" + new BigDecimal("100.45")));
		this.accountsService.clearAccounts();
	}
}
