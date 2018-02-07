package com.db.awmd.challenge.web;

import java.math.BigDecimal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	
	@GetMapping("/transfer/{accountIdFrom}/{accountIdTo}/{amount}")
    public ResponseEntity<Object> transferMoney(@PathVariable String accountIdFrom, @PathVariable String accountIdTo, @PathVariable BigDecimal amount) throws Exception {
		Account accFrom = accountsService.getAccount(accountIdFrom);
		Account accTo = accountsService.getAccount(accountIdTo);
		this.accountsService.transferMoney(accFrom, accTo, amount);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
	// withdraw specified amount from the specified account number
	@GetMapping("/withdraw/{accountId}/amount/{amount}")
    public ResponseEntity<Object> withdraw(@PathVariable String accountId, @PathVariable BigDecimal amount) throws Exception {
		Account acc = accountsService.getAccount(accountId);
    	this.accountsService.withdraw(amount, acc);
    	return new ResponseEntity<>(HttpStatus.OK);
    }

    // deposit amount in specified account.
	@GetMapping("/deposit/{accountId}/{amount}")
    public ResponseEntity<Object> deposit(@PathVariable String accountId, @PathVariable BigDecimal amount) throws Exception {
		Account acc = accountsService.getAccount(accountId);
    	this.accountsService.deposit(acc, amount);
    	return new ResponseEntity<>(HttpStatus.OK);
    }
	
}
