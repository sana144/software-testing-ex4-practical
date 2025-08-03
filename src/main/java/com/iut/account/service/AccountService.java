package com.iut.account.service;

import com.iut.Repository;
import com.iut.account.model.Account;
import com.iut.account.repo.AccountRepository;

import java.util.List;

public class AccountService {

    private final Repository<Account, String> repository;

    public AccountService(Repository<Account, String> repository) {
        this.repository = repository;
    }

    public boolean createAccount(String id, int initialBalance, String userId) {
        if (repository.existsById(id)) {
            return false;
        }
        Account account = new Account(id, initialBalance);
        account.setUserId(userId);
        return repository.save(account);
    }

    public boolean deposit(String accountId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (repository.existsById(accountId)) {
            Account account = repository.findById(accountId);
            account.setBalance(account.getBalance() + amount);
            return repository.update(account);
        }
        return false;
    }

    public boolean withdraw(String accountId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (repository.existsById(accountId)) {
            Account account = repository.findById(accountId);
            if (account.getBalance() < amount) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            account.setBalance(account.getBalance() - amount);
            return repository.update(account);
        }
        return false;
    }

    public boolean transfer(String fromId, String toId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (repository.existsById(fromId) && repository.existsById(toId)) {
            Account from = repository.findById(fromId);
            Account to = repository.findById(toId);
            if (from.getBalance() < amount) {
                throw new IllegalArgumentException("Insufficient funds in source account");
            }
            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);
            repository.update(from);
            repository.update(to);
            return true;
        }
        return false;
    }

    public int getBalance(String id) {
        if (repository.existsById(id)) {
            Account account = repository.findById(id);
            return account.getBalance();
        }
        throw new IllegalArgumentException("User not found");
    }

    public boolean deleteAccount(final String id) {
        return repository.delete(id);
    }

    public Account getAccount(String id) {
        return repository.findById(id);
    }

    public List<Account> getUserAccounts(String userId) {
        return ((AccountRepository) repository).findByUserId(userId);
    }

}