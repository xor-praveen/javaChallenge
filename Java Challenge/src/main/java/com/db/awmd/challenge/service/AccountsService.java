package com.db.awmd.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public void clearAccounts() {
		this.accountsRepository.clearAccounts();
	}
	
	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}
	
	public void transferMoney(Account fromAcc, Account toAcc, BigDecimal amount) throws Exception {
		this.accountsRepository.transferMoney(fromAcc, toAcc, amount);
		
	}

	public void withdraw(BigDecimal amount, Account acc) throws Exception {
		this.accountsRepository.withdraw(amount, acc);
	}

	public void deposit(Account acc, BigDecimal amount) throws Exception {
		this.accountsRepository.deposit(amount, acc);
	}
}
