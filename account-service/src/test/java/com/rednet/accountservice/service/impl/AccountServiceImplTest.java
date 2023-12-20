package com.rednet.accountservice.service.impl;

import com.rednet.accountservice.dto.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountService    accountService = new AccountServiceImpl(accountRepository);

    private final long      expectedID = 123;
    private final String    expectedUsername = "username";
    private final String    expectedEmail = "email";
    private final String    expectedPassword = "password";
    private final String    expectedSecretWord = "secretWord";
    private final String    expectedUpdatedUsername = "usernameUpdated";
    private final String    expectedUpdatedEmail = "emailUpdated";
    private final String    expectedUpdatedPassword = "passwordUpdated";
    private final String    expectedUpdatedSecretWord = "secretWordUpdated";
    private final String[]  expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};
    private final String[]  expectedRoles = new String[] {"ROLE_USER"};

    @AfterEach
    public void after() {
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void createAccount() {
        AccountCreationBody accountCreationBody = new AccountCreationBody(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            expectedRoles
        );

        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenReturn(expectedAccount);

        assertDoesNotThrow(() -> {
            Account actualAccount = accountService.createAccount(accountCreationBody);
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getID).toArray(String[]::new);

            assertEquals(expectedID, actualAccount.getID());
            assertEquals(expectedUsername, actualAccount.getUsername());
            assertEquals(expectedEmail, actualAccount.getEmail());
            assertEquals(expectedPassword, actualAccount.getPassword());
            assertEquals(expectedSecretWord, actualAccount.getSecretWord());
            assertTrue(compareStringArrayContent(expectedRoles, actualRoles));
        });

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));

        verify(accountRepository).save(argThat(account ->
            account.getUsername().equals(expectedUsername) &&
            account.getEmail().equals(expectedEmail) &&
            account.getPassword().equals(expectedPassword) &&
            account.getSecretWord().equals(expectedSecretWord) &&

            compareStringArrayContent(
                expectedRoles,
                account.getRoles().stream().map(Role::getID).toArray(String[]::new)
            )
        ));
    }

    @Test
    void createAccount_OccupiedValue() {
        AccountCreationBody accountCreationBody = new AccountCreationBody(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            expectedRoles
        );

        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );


        when(accountRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(expectedAccount));

        assertThrows(OccupiedValueException.class, () -> accountService.createAccount(accountCreationBody));

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void updateAccount_UsernameAndEmailUniqueValidation() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUpdatedUsername,
            expectedUpdatedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.findByUsernameOrEmail(any(),any())).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenReturn(updatedAccount);

        assertDoesNotThrow(() -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByUsernameOrEmail(eq(expectedUpdatedUsername), eq(expectedUpdatedEmail));

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUpdatedUsername) &&
            account.getEmail().equals(expectedUpdatedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getID).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_UsernameAndEmailUniqueValidation_OccupiedValue() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUpdatedUsername,
            expectedUpdatedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
            expectedUpdatedUsername,
            expectedUpdatedEmail,
            "expectedUpdatedPassword",
            "expectedUpdatedSecretWord",
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        existingAccount.setID(111);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.findByUsernameOrEmail(any(),any())).thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByUsernameOrEmail(eq(expectedUpdatedUsername), eq(expectedUpdatedEmail));
    }

    @Test
    void updateAccount_UsernameUniqueValidation() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUpdatedUsername,
            expectedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.save(any())).thenReturn(updatedAccount);

        assertDoesNotThrow(() -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByUsername(eq(expectedUpdatedUsername));

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUpdatedUsername) &&
            account.getEmail().equals(expectedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getID).toArray(String[]::new)
           )
        ));
    }

    @Test
    void updateAccount_UsernameUniqueValidation_OccupiedValue() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUpdatedUsername,
            expectedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
            expectedUpdatedUsername,
            "expectedEmail",
            "expectedUpdatedPassword",
            "expectedUpdatedSecretWord",
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        existingAccount.setID(111);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.findByUsername(any())).thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByUsername(eq(expectedUpdatedUsername));
    }

    @Test
    void updateAccount_EmailUniqueValidation() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUsername,
            expectedUpdatedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByEmail(eq(expectedUpdatedEmail));

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUsername) &&
            account.getEmail().equals(expectedUpdatedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
               expectedUpdatedRoles,
               account.getRoles().stream().map(Role::getID).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_EmailUniqueValidation_OccupiedValue() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUsername,
            expectedUpdatedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
            "expectedUsername",
            expectedUpdatedEmail,
            "expectedUpdatedPassword",
            "expectedUpdatedSecretWord",
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        existingAccount.setID(111);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.findByEmail(any())).thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).findByEmail(eq(expectedUpdatedEmail));
    }

    @Test
    void updateAccount_WithoutUsernameAndEmailUpdating() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedUpdatedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));
        when(accountRepository.save(any())).thenReturn(updatedAccount);

        assertDoesNotThrow(() -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedID));

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUsername) &&
            account.getEmail().equals(expectedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getID).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_NotFound() {
        long expectedInvalidID = 122;

        Account updatedAccount = new Account(
            expectedUpdatedUsername,
            expectedEmail,
            expectedUpdatedPassword,
            expectedUpdatedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        updatedAccount.setID(expectedInvalidID);

        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(updatedAccount));

        verify(accountRepository).findById(eq(expectedInvalidID));
    }

    @Test
    void getAccountByID() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> {
            Account actualAccount = accountService.getAccountByID(expectedID);
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getID).toArray(String[]::new);

            assertEquals(expectedID, actualAccount.getID());
            assertEquals(expectedUsername, actualAccount.getUsername());
            assertEquals(expectedEmail, actualAccount.getEmail());
            assertEquals(expectedPassword, actualAccount.getPassword());
            assertEquals(expectedSecretWord, actualAccount.getSecretWord());
            assertTrue(compareStringArrayContent(expectedRoles, actualRoles));
        });

        verify(accountRepository).findById(eq(expectedID));
    }

    @Test
    void getAccountByID_NotFound() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
    }

    @Test
    void getAccountByUsernameOrEmail() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> {
            Account actualAccount = accountService.getAccountByUsernameOrEmail(expectedUsername, expectedEmail);
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getID).toArray(String[]::new);

            assertEquals(expectedID, actualAccount.getID());
            assertEquals(expectedUsername, actualAccount.getUsername());
            assertEquals(expectedEmail, actualAccount.getEmail());
            assertEquals(expectedPassword, actualAccount.getPassword());
            assertEquals(expectedSecretWord, actualAccount.getSecretWord());
            assertTrue(compareStringArrayContent(expectedRoles, actualRoles));
        });

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void getAccountByUsernameOrEmail_NotFound() {
        when(accountRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.empty());

        assertThrows(
            AccountNotFoundException.class,
            () -> accountService.getAccountByUsernameOrEmail(expectedUsername, expectedEmail)
        );

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void deleteAccountByID() {
        Account expectedAccount = new Account("","","","", List.of());

        expectedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> accountService.deleteAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).delete(argThat(account -> account.getID() == expectedID));
    }

    @Test
    void deleteAccountByID_NotFound() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
    }

    @Test
    void getAccountByUsername() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsername(any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> {
            Account actualAccount = accountService.getAccountByUsername(expectedUsername);
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getID).toArray(String[]::new);

            assertEquals(expectedID, actualAccount.getID());
            assertEquals(expectedUsername, actualAccount.getUsername());
            assertEquals(expectedEmail, actualAccount.getEmail());
            assertEquals(expectedPassword, actualAccount.getPassword());
            assertEquals(expectedSecretWord, actualAccount.getSecretWord());
            assertTrue(compareStringArrayContent(expectedRoles, actualRoles));
        });

        verify(accountRepository).findByUsername(eq(expectedUsername));
    }

    @Test
    void getAccountByUsername_NotFound() {
        when(accountRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByUsername(expectedUsername));

        verify(accountRepository).findByUsername(eq(expectedUsername));
    }

    @Test
    void getAccountByEmail() {
        Account expectedAccount = new Account(
            expectedUsername,
            expectedEmail,
            expectedPassword,
            expectedSecretWord,
            Arrays.stream(expectedRoles).map(Role::new).toList()
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByEmail(any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> {
            Account actualAccount = accountService.getAccountByEmail(expectedEmail);
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getID).toArray(String[]::new);

            assertEquals(expectedID, actualAccount.getID());
            assertEquals(expectedUsername, actualAccount.getUsername());
            assertEquals(expectedEmail, actualAccount.getEmail());
            assertEquals(expectedPassword, actualAccount.getPassword());
            assertEquals(expectedSecretWord, actualAccount.getSecretWord());
            assertTrue(compareStringArrayContent(expectedRoles, actualRoles));
        });

        verify(accountRepository).findByEmail(eq(expectedEmail));
    }

    @Test
    void getAccountByEmail_NotFound() {
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByEmail(expectedEmail));

        verify(accountRepository).findByEmail(eq(expectedEmail));
    }

    @Test
    void existsAccountByUsername() {
        when(accountRepository.existsByUsername(any())).thenReturn(true);

        assertTrue(accountService.existsAccountByUsername(expectedUsername));

        verify(accountRepository).existsByUsername(eq(expectedUsername));
    }

    @Test
    void existsAccountByUsername_NotFound() {
        when(accountRepository.existsByUsername(any())).thenReturn(false);

        assertFalse(accountService.existsAccountByUsername(expectedUsername));

        verify(accountRepository).existsByUsername(eq(expectedUsername));
    }

    @Test
    void existsAccountByEmail() {
        when(accountRepository.existsByEmail(any())).thenReturn(true);

        assertTrue(accountService.existsAccountByEmail(expectedEmail));

        verify(accountRepository).existsByEmail(eq(expectedEmail));
    }

    @Test
    void existsAccountByEmail_NotFound() {
        when(accountRepository.existsByEmail(any())).thenReturn(false);

        assertFalse(accountService.existsAccountByEmail(expectedEmail));

        verify(accountRepository).existsByEmail(eq(expectedEmail));
    }

    private boolean compareStringArrayContent(String[] expectedArray, String[] actualArray) {
        if (expectedArray.length != actualArray.length) return false;

        for (String expectedItem : expectedArray) {
            if (Arrays.stream(actualArray).noneMatch(actualItem -> actualItem.equals(expectedItem))) return false;
        }

        return true;
    }
}