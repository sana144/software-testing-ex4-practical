package com.iut;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iut.account.model.Account;
import com.iut.account.service.AccountService;

import org.junit.jupiter.api.Assertions;
import java.util.*;

public class AccountServiceTest {

    private Repository<Account, String> repository;
    private AccountService accountService;

    // Fake Repository (In-Memory)
    class FakeAccountRepository implements Repository<Account, String> {
        private final Map<String, Account> storage = new HashMap<>();

        @Override
        public boolean save(Account input) {
            if (storage.containsKey(input.getId()))
                return false;
            storage.put(input.getId(), input);
            return true;
        }

        @Override
        public boolean update(Account input) {
            if (!storage.containsKey(input.getId()))
                return false;
            storage.put(input.getId(), input);
            return true;
        }

        @Override
        public boolean delete(String id) {
            return storage.remove(id) != null;
        }

        @Override
        public boolean existsById(String id) {
            return storage.containsKey(id);
        }

        @Override
        public Account findById(String id) {
            return storage.get(id);
        }

        @Override
        public List<Account> findAll() {
            return new ArrayList<>(storage.values());
        }
    }

    @BeforeEach
    void setup() {
        repository = new FakeAccountRepository();
        accountService = new AccountService(repository);
    }

    @Test
    void createAccountTest() {
        Assertions.assertTrue(accountService.createAccount("A1", 1000, "U1"));
        Assertions.assertFalse(accountService.createAccount("A1", 1000, "U1")); // Duplicate ID
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Account("A2", -100, "U2"));
    }

    @Test
    void depositTest() {
        accountService.createAccount("A2", 500, "U2");

        Assertions.assertTrue(accountService.deposit("A2", 200));
        Assertions.assertEquals(700, accountService.getBalance("A2"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.deposit("A2", -100));

        // Assertions.assertThrows(IllegalArgumentException.class, () ->
        // accountService.deposit("nonexistent", 100));
        Assertions.assertFalse(accountService.deposit("nonexistent", 100));

    }

    @Test
    void withdrawTest() {
        accountService.createAccount("A3", 1000, "U3");

        Assertions.assertTrue(accountService.withdraw("A3", 400));
        Assertions.assertEquals(600, accountService.getBalance("A3"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.withdraw("A3", 700));
        Assertions.assertEquals(600, accountService.getBalance("A3"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.withdraw("A3", -200));
        // Assertions.assertThrows(IllegalArgumentException.class, () ->
        // accountService.withdraw("nonexistent", 100));
        Assertions.assertFalse(accountService.withdraw("nonexistent", 100));

    }

    @Test
    void transferTest() {
        final String fromId = "A4";
        final String toId = "A5";
        accountService.createAccount(fromId, 1000, "U4");
        accountService.createAccount(toId, 500, "U5");

        Assertions.assertTrue(accountService.transfer(fromId, toId, 300));
        Assertions.assertEquals(700, accountService.getBalance(fromId));
        Assertions.assertEquals(800, accountService.getBalance(toId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.transfer(fromId, toId, 800));
        Assertions.assertEquals(700, accountService.getBalance(fromId));
        Assertions.assertEquals(800, accountService.getBalance(toId));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.transfer(fromId, toId, -100));
        // Assertions.assertThrows(IllegalArgumentException.class, () ->
        // accountService.transfer(fromId, "nonexistent", 100));
        // Assertions.assertThrows(IllegalArgumentException.class, () ->
        // accountService.transfer("nonexistent", toId, 100));
        Assertions.assertFalse(accountService.transfer(fromId, "nonexistent", 100));
        Assertions.assertFalse(accountService.transfer("nonexistent", toId, 100));

    }

    @Test
    void getBalanceTest() {
        final String id = "A6";
        accountService.createAccount(id, 1200, "U6");

        Assertions.assertEquals(1200, accountService.getBalance(id));
        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.getBalance("nonexistent"));
    }

    @Test
    void existsAndGetAccountTest() {
        final String id = "A7";
        accountService.createAccount(id, 800, "U7");

        Assertions.assertTrue(repository.existsById(id));
        Assertions.assertFalse(repository.existsById("nonexistent"));

        Assertions.assertNotNull(accountService.getAccount(id));
        Assertions.assertNull(accountService.getAccount("nonexistent"));

        Assertions.assertEquals(800, accountService.getAccount(id).getBalance());
    }

    @Test
    void updateNonexistentAccountTest() {
        Account ghost = new Account("ghost", 1000, "U8");
        Assertions.assertFalse(repository.update(ghost));
    }

    @Test
    void accountConstructorAndToStringTest() {
        Account account = new Account("toStrTest", 200, "U9");
        String expected = "Account{" +
                "id='toStrTest'" +
                ", balance=200" +
                ", userId='U9'" +
                '}';
        Assertions.assertEquals(expected, account.toString());

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Account("bad", -1, "U10"));
    }

    @Test
    void getAllAccountsTest() {
        accountService.createAccount("A8", 500, "U11");
        accountService.createAccount("A9", 700, "U12");

        Assertions.assertEquals(2, repository.findAll().size());
        Assertions.assertTrue(repository.findAll().stream().anyMatch(a -> a.getId().equals("A8")));
        Assertions.assertTrue(repository.findAll().stream().anyMatch(a -> a.getId().equals("A9")));
    }

    @Test
    void deleteAccountTest() {
        final String id = "A10";

        accountService.createAccount(id, 1000, "U10");

        Assertions.assertNotNull(accountService.getAccount(id));
        Assertions.assertTrue(accountService.deleteAccount(id));
        Assertions.assertNull(accountService.getAccount(id));

        // Delete nonexistent account
        Assertions.assertFalse(accountService.deleteAccount("ghost"));
    }

    // @Test
    // void getUserAccountsTest() {
    //     accountService.createAccount("A11", 500, "U11");
    //     accountService.createAccount("A12", 700, "U11");

    //     Assertions.assertEquals(2, accountService.getUserAccounts("U11").size());
    //     Assertions.assertTrue(accountService.getUserAccounts("U11").stream().anyMatch(a -> a.getId().equals("A11")));
    //     Assertions.assertTrue(accountService.getUserAccounts("U11").stream().anyMatch(a -> a.getId().equals("A12")));

    //     Assertions.assertTrue(accountService.getUserAccounts("U-empty").isEmpty());
    // }

}
