package com.db.awmd.challenge.repository;

import java.math.BigDecimal;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	Account getAccount(String accountId);

	void clearAccounts();

	void deposit(BigDecimal amount, Account acc) throws Exception;

	void transferMoney(Account fromAcc, Account toAcc, BigDecimal amount) throws Exception;

	void withdraw(BigDecimal amount, Account acc) throws Exception;
}
