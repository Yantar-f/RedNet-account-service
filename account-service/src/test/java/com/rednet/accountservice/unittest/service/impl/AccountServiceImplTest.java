package com.rednet.accountservice.unittest.service.impl;

import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.model.AccountCreationData;
import com.rednet.accountservice.model.AccountUniqueFields;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
import com.rednet.accountservice.service.impl.AccountServiceImpl;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final AccountRepository accountRepository = mock(AccountRepository.class);

    private final AccountService sut = new AccountServiceImpl(accountRepository);

    private final long      expectedID = Instancio.create(long.class);
    private final String    expectedUsername = Instancio.create(String.class);
    private final String    expectedEmail = Instancio.create(String.class);
    private final String    expectedPassword = Instancio.create(String.class);
    private final String    expectedSecretWord = Instancio.create(String.class);
    private final String    expectedUpdatedUsername = Instancio.create(String.class);
    private final String    expectedUpdatedEmail = Instancio.create(String.class);
    private final String    expectedUpdatedPassword = Instancio.create(String.class);
    private final String    expectedUpdatedSecretWord = Instancio.create(String.class);

    private final List<Role>  expectedUpdatedRoles = new ArrayList<>(){{add(new Role("ROLE_ADMIN"));}};
    private final List<Role>  expectedRoles = new ArrayList<>(){{add(new Role("ROLE_USER"));}};

    @Test
    void Creating_account_is_successful() {
        AccountCreationData creationData = new AccountCreationData(
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

        expectedAccount.setId(expectedID);

        when(accountRepository.findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenReturn(Optional.empty());

        when(accountRepository.save(eq(expectedUnsavedAccount)))
                .thenReturn(expectedAccount);

        Account actualAccount = sut.createAccount(creationData);

        assertEquals(expectedAccount, actualAccount);

        verify(accountRepository)
                .save(eq(expectedUnsavedAccount));
    }

    @Test
    void Creating_account_with_not_unique_email_or_username_is_not_successful() {
        AccountCreationData accountCreationData = new AccountCreationData(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles.stream().map(Role::getID).toArray(String[]::new)
        );

        List<AccountUniqueFields> expectedUniqueFields = List.of(
                new AccountUniqueFields(expectedUsername, expectedEmail)
        );

        when(accountRepository.findAllUniqueFieldsByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenReturn(expectedUniqueFields);

        assertThrows(OccupiedValueException.class, () -> sut.createAccount(accountCreationData));
    }

    @Test
    void Updating_account_is_successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setId(expectedID);

        Account expectedUpdatedAccount = new Account(
                expectedUpdatedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        expectedUpdatedAccount.setId(expectedID);

        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.of(expectedAccount));

        when(accountRepository.findByUsernameOrEmail(eq(expectedUsername),eq(expectedEmail)))
                .thenReturn(Optional.empty());

        when(accountRepository.save(eq(expectedUpdatedAccount)))
                .thenReturn(expectedUpdatedAccount);

        sut.updateAccount(expectedUpdatedAccount);

        verify(accountRepository)
                .save(eq(expectedUpdatedAccount));
    }

    @Test
    void Updating_account_with_not_unique_username_is_not_successful() {
        Account accountToUpdate = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        accountToUpdate.setId(expectedID);

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setId(expectedID);

        List<AccountUniqueFields> uniqueFields = List.of(
                new AccountUniqueFields(
                        expectedUpdatedUsername,
                        Instancio.create(String.class)
                )
        );

        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.of(accountToUpdate));

        when(accountRepository.findAllUniqueFieldsByUsernameOrEmail(eq(expectedUpdatedUsername), eq(expectedUpdatedEmail)))
                .thenReturn(uniqueFields);

        assertThrows(OccupiedValueException.class, () -> sut.updateAccount(updatedAccount));

        verify(accountRepository, never())
                .save(any());
    }

    @Test
    void Updating_account_with_not_unique_email_is_not_successful() {
        Account accountToUpdate = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        accountToUpdate.setId(expectedID);

        Account updatedAccount = new Account(
                expectedUsername,
                expectedUpdatedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedUpdatedRoles
        );

        updatedAccount.setId(expectedID);

        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.of(accountToUpdate));

        when(accountRepository.existsByEmail(eq(expectedUpdatedEmail)))
                .thenReturn(true);

        assertThrows(OccupiedValueException.class, () -> sut.updateAccount(updatedAccount));
    }

    @Test
    void Updating_account_by_invalid_id_is_not_successful() {
        long expectedInvalidID = Instancio.create(long.class);

        Account updatedAccount = new Account(
                expectedUpdatedUsername,
                expectedEmail,
                expectedUpdatedPassword,
                expectedUpdatedSecretWord,
                expectedRoles
        );

        updatedAccount.setId(expectedInvalidID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.updateAccount(updatedAccount));
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

        expectedAccount.setId(expectedID);

        when(accountRepository.findById(any()))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByID(expectedID);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void Getting_account_by_invalid_id_is_not_successful() {
        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByID(expectedID));
    }

    @Test
    void Getting_account_by_existing_username_and_email_is_successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setId(expectedID);

        when(accountRepository.findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByUsernameOrEmail(expectedUsername, expectedEmail);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void Getting_account_by_not_existing_username_and_email_is_not_successful() {
        when(accountRepository.findByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenReturn(Optional.empty());

        assertThrows(
            AccountNotFoundException.class,
            () -> sut.getAccountByUsernameOrEmail(expectedUsername, expectedEmail)
        );
    }

    @Test
    void Deleting_account_by_existing_id_is_successful() {
        Account expectedAccount = new Account("","","","", List.of());

        expectedAccount.setId(expectedID);

        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.of(expectedAccount));

        sut.deleteAccountByID(expectedID);

        verify(accountRepository)
                .delete(argThat(account -> account.getId() == expectedID));
    }

    @Test
    void Deleting_account_by_not_existing_id_is_not_auccessful() {
        when(accountRepository.findById(eq(expectedID)))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.deleteAccountByID(expectedID));

        verify(accountRepository, never())
                .deleteById(any());
    }

    @Test
    void Getting_account_by_existing_username_is_successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setId(expectedID);

        when(accountRepository.findByUsername(eq(expectedUsername)))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByUsername(expectedUsername);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void Getting_account_by_not_existing_username_is_not_successful() {
        when(accountRepository.findByUsername(eq(expectedUsername)))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByUsername(expectedUsername));
    }

    @Test
    void Getting_account_by_email_is_successful() {
        Account expectedAccount = new Account(
                expectedUsername,
                expectedEmail,
                expectedPassword,
                expectedSecretWord,
                expectedRoles
        );

        expectedAccount.setId(expectedID);

        when(accountRepository.findByEmail(eq(expectedEmail)))
                .thenReturn(Optional.of(expectedAccount));

        Account actualAccount = sut.getAccountByEmail(expectedEmail);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void Getting_account_by_not_existing_email_is_not_successful() {
        when(accountRepository.findByEmail(eq(expectedEmail)))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> sut.getAccountByEmail(expectedEmail));
    }

    @Test
    void Checking_existence_by_existing_username_is_positive() {
        when(accountRepository.existsByUsername(eq(expectedUsername)))
                .thenReturn(true);

        assertTrue(sut.existsAccountByUsername(expectedUsername));
    }

    @Test
    void Checking_existence_by_not_existing_username_is_negative() {
        when(accountRepository.existsByUsername(eq(expectedUsername)))
                .thenReturn(false);

        assertFalse(sut.existsAccountByUsername(expectedUsername));
    }

    @Test
    void Checking_existence_by_existing_email_is_positive() {
        when(accountRepository.existsByEmail(eq(expectedEmail)))
                .thenReturn(true);

        assertTrue(sut.existsAccountByEmail(expectedEmail));
    }

    @Test
    void Checking_existence_by_not_existing_email_is_negative() {
        when(accountRepository.existsByEmail(eq(expectedEmail)))
                .thenReturn(false);

        assertFalse(sut.existsAccountByEmail(expectedEmail));
    }
}