package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	public static final BigDecimal MIN_BALANCE = new BigDecimal(500);
	public static final BigDecimal MIN_AMOUNT_VALUE = new BigDecimal(1);
	public static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal(0);
	private final Object lock = new Object();

	//@Autowired
	//NotificationService notificationService;

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public synchronized void transferMoney(Account fromAcc, Account toAcc, BigDecimal amount) throws Exception {
		synchronized (lock) {
			if (fromAcc != null && toAcc != null) {
				if (amount.compareTo(NEGATIVE_AMOUNT) > 0 && amount.compareTo(MIN_AMOUNT_VALUE) > 0) {
					if (fromAcc.getBalance().compareTo(MIN_AMOUNT_VALUE) > 0) {
						if (fromAcc.getBalance().compareTo(amount) > 0 || fromAcc.getBalance().subtract(amount).compareTo(MIN_BALANCE) > 0) {
							
							log.info("Before Transfer, Current Account Balance in Account {} is {} ", fromAcc.getAccountId(), fromAcc.getBalance());
							log.info("Before Transfer, Current Account Balance in Account {} is {} ", toAcc.getAccountId(), toAcc.getBalance());
							
							toAcc.setBalance(toAcc.getBalance().add(amount));
							fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
								
							log.info("Transfer Successful From Account {}, To Account {} For Amount {} ", fromAcc.getAccountId(), toAcc.getAccountId(), amount);
							log.info("After Transfer, Current Account Balance {} in Account Number {} ", fromAcc.getBalance(), fromAcc.getAccountId());
							log.info("After Transfer, Current Account Balance {} in Account Number {} ", toAcc.getBalance(), toAcc.getAccountId());
						} else {
							log.info("Insufficient funds in Account Number: " + fromAcc.getAccountId());
						}
					} else {
						log.info("Please maintain minimum balance:.. " + fromAcc.getAccountId());
					}
				} else {
					log.info("Amount provided for tansfer should be greater than 0");
				}
			} else {
				log.info("Invalid Account Provided");
			}
		}
	}

	@Override
	public synchronized void deposit(BigDecimal amount, Account account) throws Exception {
		synchronized (lock) {
			if (account != null) {
				if (amount.compareTo(NEGATIVE_AMOUNT) > 0) {
					log.info("Current Account Balance Before Deposit in Account {} is {}", account.getAccountId(), account.getBalance());
					log.info("Depositing Amount {} in Account {} ", amount, account.getAccountId());
					account.setBalance(account.getBalance().add(amount));
					log.info("Current Account Balance After Deposit in Account {} is {}", account.getAccountId(), account.getBalance());
				} else {
					log.info("Invalid Amount provided");
				}
			} else {
				log.info("Invalid Account Provided");
			}
		}
	}

	@Override
	public synchronized void withdraw(BigDecimal amount, Account account) throws Exception {
		synchronized (lock) {
			if (account != null) {
				if (amount.compareTo(NEGATIVE_AMOUNT) > 0) {
					if (account.getBalance().compareTo(MIN_BALANCE) > 0) {
						log.info("Current Account Balance Before Withdraw in Account {} is {}", account.getAccountId(), account.getBalance());
						log.info("Withdrawing Amount {} from Account {} ", amount, account.getAccountId());
						account.setBalance(account.getBalance().subtract(amount));
						log.info("Current Account Balance After Withdraw in Account {} is {}", account.getAccountId(), account.getBalance());
					} else {
						log.info("Insufficient funds in Account {}", account.getAccountId());
					}
				} else {
					log.info("Invalid Amount provided");
				}
			} else {
				log.info("Invalid Account");
			}
		}
	}
}
