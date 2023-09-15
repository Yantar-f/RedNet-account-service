package com.rednet.accountservice.service.impl;

import com.rednet.accountservice.dto.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceImplTest {
    private AccountRepository accountRepository = mock(AccountRepository.class);
    private AccountService accountService = new AccountServiceImpl(accountRepository);

    @Test
    void createAccount() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getDesignation).toArray(String[]::new);

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
                account.getRoles().stream().map(Role::getDesignation).toArray(String[]::new)
            )
        ));
    }

    @Test
    void createAccount_OccupiedValue() {
        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_UsernameAndEmailUniqueValidation() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedUpdatedUsername = "usernameUpdated",
            expectedEmail = "email",
            expectedUpdatedEmail = "emailUpdated",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};


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
        verify(accountRepository, never()).findByEmail(any());
        verify(accountRepository, never()).findByUsername(any());

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUpdatedUsername) &&
            account.getEmail().equals(expectedUpdatedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getDesignation).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_UsernameAndEmailUniqueValidation_OccupiedValue() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedUpdatedUsername = "usernameUpdated",
            expectedEmail = "email",
            expectedUpdatedEmail = "emailUpdated",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByEmail(any());
        verify(accountRepository, never()).findByUsername(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_UsernameUniqueValidation() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedUpdatedUsername = "usernameUpdated",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());
        verify(accountRepository, never()).findByEmail(any());

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUpdatedUsername) &&
            account.getEmail().equals(expectedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getDesignation).toArray(String[]::new)
           )
        ));
    }

    @Test
    void updateAccount_UsernameUniqueValidation_OccupiedValue() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedUpdatedUsername = "usernameUpdated",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByEmail(any());
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_EmailUniqueValidation() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedUpdatedEmail = "emailUpdated",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByUsername(any());
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUsername) &&
            account.getEmail().equals(expectedUpdatedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
               expectedUpdatedRoles,
               account.getRoles().stream().map(Role::getDesignation).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_EmailUniqueValidation_OccupiedValue() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedUpdatedEmail = "emailUpdated",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByUsername(any());
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_WithoutUsernameAndEmailUpdating() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedUpdatedPassword = "passwordUpdated",
            expectedSecretWord = "secretWord",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[]
            expectedRoles = new String[] {"ROLE_USER"},
            expectedUpdatedRoles = new String[] {"ROLE_ADMIN"};

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
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());
        verify(accountRepository, never()).findByUsername(any());
        verify(accountRepository, never()).findByEmail(any());

        verify(accountRepository).save(argThat(account ->
            account.getID() == expectedID &&
            account.getUsername().equals(expectedUsername) &&
            account.getEmail().equals(expectedEmail) &&
            account.getPassword().equals(expectedUpdatedPassword) &&
            account.getSecretWord().equals(expectedUpdatedSecretWord) &&

            compareStringArrayContent(
                expectedUpdatedRoles,
                account.getRoles().stream().map(Role::getDesignation).toArray(String[]::new)
            )
        ));
    }

    @Test
    void updateAccount_NotFound() {
        long expectedInvalidID = 122;

        String
            expectedUsername = "username",
            expectedUpdatedUsername = "usernameUpdated",
            expectedEmail = "email",
            expectedUpdatedPassword = "passwordUpdated",
            expectedUpdatedSecretWord = "secretWordUpdated";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
        verify(accountRepository, never()).findByUsernameOrEmail(any(), any());
        verify(accountRepository, never()).findByEmail(any());
        verify(accountRepository, never()).findByUsername(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountByID() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getDesignation).toArray(String[]::new);

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
        long expectedID = 123;

        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
    }

    @Test
    void getAccountByUsernameOrEmail() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getDesignation).toArray(String[]::new);

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
        String
            expectedUsername = "username",
            expectedEmail = "email";

        when(accountRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.empty());

        assertThrows(
            AccountNotFoundException.class,
            () -> accountService.getAccountByUsernameOrEmail(expectedUsername, expectedEmail)
        );

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void deleteAccountByID() {
        long expectedID = 123;

        Account expectedAccount = new Account("","","","", List.of());

        expectedAccount.setID(expectedID);

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));

        assertDoesNotThrow(() -> accountService.deleteAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository).delete(argThat(account -> account.getID() == expectedID));
    }

    @Test
    void deleteAccountByID_NotFound() {
        long expectedID = 123;

        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccountByID(expectedID));

        verify(accountRepository).findById(eq(expectedID));
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void getAccountByUsername() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getDesignation).toArray(String[]::new);

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
        String expectedUsername = "username";

        when(accountRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByUsername(expectedUsername));

        verify(accountRepository).findByUsername(eq(expectedUsername));
    }

    @Test
    void getAccountByEmail() {
        long expectedID = 123;

        String
            expectedUsername = "username",
            expectedEmail = "email",
            expectedPassword = "password",
            expectedSecretWord = "secretWord";

        String[] expectedRoles = new String[] {"ROLE_USER"};

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
            String[] actualRoles = actualAccount.getRoles().stream().map(Role::getDesignation).toArray(String[]::new);

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
        String expectedEmail = "email";

        when(accountRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByUsername(expectedEmail));

        verify(accountRepository).findByEmail(eq(expectedEmail));
    }

    @Test
    void existsAccountByUsername() {
        String expectedUsername = "username";

        when(accountRepository.existsByUsername(any())).thenReturn(true);

        assertTrue(accountService.existsAccountByUsername(expectedUsername));

        verify(accountRepository).existsByUsername(eq(expectedUsername));
    }

    @Test
    void existsAccountByUsername_NotFound() {
        String expectedUsername = "username";

        when(accountRepository.existsByUsername(any())).thenReturn(false);

        assertFalse(accountService.existsAccountByUsername(expectedUsername));

        verify(accountRepository).existsByUsername(eq(expectedUsername));
    }

    @Test
    void existsAccountByEmail() {
        String expectedEmail = "email";

        when(accountRepository.existsByEmail(any())).thenReturn(true);

        assertTrue(accountService.existsAccountByEmail(expectedEmail));

        verify(accountRepository).existsByEmail(eq(expectedEmail));
    }

    @Test
    void existsAccountByEmail_NotFound() {
        String expectedEmail = "email";

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