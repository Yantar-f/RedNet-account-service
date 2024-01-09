package com.rednet.accountservice.service.impl;

import com.rednet.accountservice.dto.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;

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
    Random  rand = new Random();
    int     stringLengthBound = 200;
    int     IDValueBound = 200;

    private final AccountRepository accountRepository = mock(AccountRepository.class);

    private final AccountService sut = new AccountServiceImpl(accountRepository);

    private final long      expectedID          = rand.nextLong(IDValueBound);
    private final String    expectedUsername    = randString();
    private final String    expectedEmail       = randString();
    private final String    expectedPassword    = randString();
    private final String    expectedSecretWord          = randString();
    private final String    expectedUpdatedUsername     = randString();
    private final String    expectedUpdatedEmail        = randString();
    private final String    expectedUpdatedPassword     = randString();
    private final String    expectedUpdatedSecretWord   = randString();

    private final List<Role>  expectedUpdatedRoles  = new ArrayList<>(){{add(new Role("ROLE_ADMIN"));}};
    private final List<Role>  expectedRoles         = new ArrayList<>(){{add(new Role("ROLE_USER"));}};

    @AfterEach
    public void after() {
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void Creating_Account_With_Unique_Email_And_Username_Is_Successful() {
        AccountCreationBody accountCreationBody = new AccountCreationBody(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles.stream().map(Role::getID).toArray(String[]::new)
        );

        Account expectedUnsavedAccount = new Account(
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
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsernameOrEmail(any(), any()))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any()))
                .thenReturn(expectedAccount);

        Account actualAccount = sut.createAccount(accountCreationBody);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository).findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
        verify(accountRepository).save(eq(expectedUnsavedAccount));
    }

    @Test
    void Creating_Account_With_Not_Unique_Email_Or_Username_Is_Not_Successful() {
        AccountCreationBody accountCreationBody = new AccountCreationBody(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles.stream().map(Role::getID).toArray(String[]::new)
        );

        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );


        when(accountRepository.findByUsernameOrEmail(any(), any()))
                .thenReturn(Optional.of(expectedAccount));

        assertThrows(OccupiedValueException.class, () -> sut.createAccount(accountCreationBody));

        verify(accountRepository)
                .findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void Updating_Account_With_Unique_Username_And_Email_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account expectedUpdatedAccount = new Account(
                expectedUpdatedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        expectedUpdatedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByUsernameOrEmail(any(),any()))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any()))
                .thenReturn(expectedUpdatedAccount);

        assertDoesNotThrow(() -> sut.updateAccount(expectedUpdatedAccount));

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByUsernameOrEmail(eq(expectedUpdatedUsername), eq(expectedUpdatedEmail));

        verify(accountRepository)
                .save(eq(expectedAccount));
    }

    @Test
    void Updating_Account_With_Not_Unique_Username_And_Email_Is_Not_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
                expectedUpdatedUsername,
                expectedUpdatedEmail,
                randString(),
                randString(),
                expectedUpdatedRoles
        );

        existingAccount.setID(IDValueBound);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByUsernameOrEmail(any(),any()))
                .thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> sut.updateAccount(updatedAccount));

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByUsernameOrEmail(eq(expectedUpdatedUsername), eq(expectedUpdatedEmail));
    }

    @Test
    void Updating_Account_With_Unique_Username_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.save(any()))
                .thenReturn(updatedAccount);

        sut.updateAccount(updatedAccount);

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByUsername(eq(expectedUpdatedUsername));

        verify(accountRepository)
                .save(eq(updatedAccount));
    }

    @Test
    void Updating_Account_With_Not_Unique_Username_Is_Not_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
                expectedUpdatedUsername,
                randString(),
                randString(),
                randString(),
                expectedUpdatedRoles
        );

        existingAccount.setID(111);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByUsername(any()))
                .thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> sut.updateAccount(updatedAccount));

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByUsername(eq(expectedUpdatedUsername));
    }

    @Test
    void Updating_Account_With_Unique_Email_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        sut.updateAccount(updatedAccount);

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByEmail(eq(expectedUpdatedEmail));

        verify(accountRepository)
                .save(eq(updatedAccount));
    }

    @Test
    void Updating_Account_With_Not_Unique_Email_Is_Not_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        Account existingAccount = new Account(
                randString(),
                expectedUpdatedEmail,
                randString(),
                randString(),
                expectedUpdatedRoles
        );

        existingAccount.setID(111);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByEmail(any()))
                .thenReturn(Optional.of(existingAccount));

        assertThrows(OccupiedValueException.class, () -> sut.updateAccount(updatedAccount));

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .findByEmail(eq(expectedUpdatedEmail));
    }

    @Test
    void Updating_Account_Without_Username_And_Email_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        Account updatedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.save(any()))
                .thenReturn(updatedAccount);

        sut.updateAccount(updatedAccount);

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .save(eq(updatedAccount));
    }

    @Test
    void Updating_Account_By_Invalid_ID_Is_Not_Successful() {
        long expectedInvalidID = rand.nextLong();

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedRoles
        );

        updatedAccount.setID(expectedInvalidID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.updateAccount(updatedAccount));

        verify(accountRepository)
                .findById(eq(expectedInvalidID));
    }

    @Test
    void Getting_Account_By_Valid_ID_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByID(expectedID);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository)
                .findById(eq(expectedID));
    }

    @Test
    void Getting_Account_By_Invalid_ID_Is_Not_Successful() {
        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByID(expectedID));

        verify(accountRepository)
                .findById(eq(expectedID));
    }

    @Test
    void Getting_Account_By_Existing_Username_And_Email_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsernameOrEmail(any(), any()))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByUsernameOrEmail(expectedUsername, expectedEmail);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository)
                .findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void Getting_Account_By_Not_Existing_Username_And_Email_Is_Not_Successful() {
        when(accountRepository.findByUsernameOrEmail(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(
            AccountNotFoundException.class,
            () -> sut.getAccountByUsernameOrEmail(expectedUsername, expectedEmail)
        );

        verify(accountRepository)
                .findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail));
    }

    @Test
    void Deleting_Account_By_Existing_ID_Is_Successful() {
        Account expectedAccount = new Account("","","","", List.of());

        expectedAccount.setID(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        sut.deleteAccountByID(expectedID);

        verify(accountRepository)
                .findById(eq(expectedID));

        verify(accountRepository)
                .delete(argThat(account -> account.getID() == expectedID));
    }

    @Test
    void Deleting_Account_By_Not_Existing_ID_Is_Not_Successful() {
        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.deleteAccountByID(expectedID));

        verify(accountRepository)
                .findById(eq(expectedID));
    }

    @Test
    void Getting_Account_By_Existing_Username_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByUsername(any()))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByUsername(expectedUsername);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository)
                .findByUsername(eq(expectedUsername));
    }

    @Test
    void Getting_Account_By_Not_Existing_Username_Is_Not_Successful() {
        when(accountRepository.findByUsername(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByUsername(expectedUsername));

        verify(accountRepository)
                .findByUsername(eq(expectedUsername));
    }

    @Test
    void Getting_Account_By_Existing_Email_Is_Successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setID(expectedID);

        when(accountRepository.findByEmail(any()))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByEmail(expectedEmail);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository)
                .findByEmail(eq(expectedEmail));
    }

    @Test
    void Getting_Account_By_Not_Existing_Email_Is_Not_Successful() {
        when(accountRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByEmail(expectedEmail));

        verify(accountRepository)
                .findByEmail(eq(expectedEmail));
    }

    @Test
    void Checking_Existence_By_Existing_Username_Is_Positive() {
        when(accountRepository.existsByUsername(any()))
                .thenReturn(true);

        assertTrue(sut.existsAccountByUsername(expectedUsername));

        verify(accountRepository)
                .existsByUsername(eq(expectedUsername));
    }

    @Test
    void Checking_Existence_By_Not_Existing_Username_Is_Negative() {
        when(accountRepository.existsByUsername(any()))
                .thenReturn(false);

        assertFalse(sut.existsAccountByUsername(expectedUsername));

        verify(accountRepository)
                .existsByUsername(eq(expectedUsername));
    }

    @Test
    void Checking_Existence_By_Existing_Email_Is_Positive() {
        when(accountRepository.existsByEmail(any()))
                .thenReturn(true);

        assertTrue(sut.existsAccountByEmail(expectedEmail));

        verify(accountRepository)
                .existsByEmail(eq(expectedEmail));
    }

    @Test
    void Checking_Existence_By_Not_Existing_Email_Is_Negative() {
        when(accountRepository.existsByEmail(any()))
                .thenReturn(false);

        assertFalse(sut.existsAccountByEmail(expectedEmail));

        verify(accountRepository)
                .existsByEmail(eq(expectedEmail));
    }

    private int randStringLength() {
        return rand.nextInt(stringLengthBound - 1) + 1;
    }

    private String randString() {
        return RandomStringUtils.random(randStringLength());
    }
}