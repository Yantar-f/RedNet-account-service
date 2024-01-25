package com.rednet.accountservice.service.impl;

import com.rednet.accountservice.model.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.model.AccountUniqueFields;
import com.rednet.accountservice.model.AccountUniqueFieldsOccupancy;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final Map<String, ConstraintsUpdatingChecker> constraintsUpdatingCheckerMap = new HashMap<>();


    @FunctionalInterface
    private interface ConstraintsUpdatingChecker {
        void checkViolation(Account account);
    }

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        initializeConstraintsUpdatingCheckerMap();
    }

    @Override
    public Account createAccount(AccountCreationBody accountCreationBody) {
        AccountUniqueFields uniqueFields = new AccountUniqueFields(
                accountCreationBody.username(),
                accountCreationBody.email()
        );

        checkAccountUniqueFieldsOccupancy(uniqueFields);

        return accountRepository.save(new Account(
                accountCreationBody.username(),
                accountCreationBody.email(),
                accountCreationBody.password(),
                accountCreationBody.secretWord(),
                Arrays.stream(accountCreationBody.roles()).map(Role::new).toList()
        ));
    }

    @Override
    public void updateAccount(Account updatedAccount) {
        Account accountToUpdate = accountRepository
                .findById(updatedAccount.getID())
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("ID", String.valueOf(updatedAccount.getID()));

                    return new AccountNotFoundException(searchFields);
                });

        checkUpdatingConstraintsViolations(accountToUpdate, updatedAccount);

        accountToUpdate.setUsername(updatedAccount.getUsername());
        accountToUpdate.setEmail(updatedAccount.getEmail());
        accountToUpdate.setPassword(updatedAccount.getPassword());
        accountToUpdate.setSecretWord(updatedAccount.getSecretWord());
        accountToUpdate.setRoles(updatedAccount.getRoles());

        accountRepository.save(accountToUpdate);
    }

    @Override
    public Account getAccountByID(long ID) {
        return accountRepository
                .findById(ID)
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("ID", String.valueOf(ID));

                    return new AccountNotFoundException(searchFields);
                });
    }

    @Override
    public Account getAccountByUsernameOrEmail(String username, String email) {
        return accountRepository
                .findByUsernameOrEmail(username, email)
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("username", username);
                    searchFields.put("email", email);

                    return new AccountNotFoundException(searchFields);
                });
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("username", username);

                    return new AccountNotFoundException(searchFields);
                });
    }

    @Override
    public Account getAccountByEmail(String email) {
        return accountRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("email", email);

                    return new AccountNotFoundException(searchFields);
                });
    }

    @Override
    public boolean existsAccountByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsAccountByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public void deleteAccountByID(long ID) {
        Account account = accountRepository
                .findById(ID)
                .orElseThrow(() -> {
                    Map<String, String> searchFields = new HashMap<>();

                    searchFields.put("ID", String.valueOf(ID));

                    return new AccountNotFoundException(searchFields);
                });

        accountRepository.delete(account);
    }

    @Override
    public AccountUniqueFieldsOccupancy getAccountUniqueFieldsOccupancy(AccountUniqueFields fields) {
        List<AccountUniqueFields> uniqueFields = accountRepository
                .findAllUniqueFieldsByUsernameOrEmail(fields.username(), fields.email());

        AccountUniqueFieldsOccupancy occupancy = new AccountUniqueFieldsOccupancy(
                fields.username(),
                fields.email(),
                false,
                false
        );

        for (var account : uniqueFields) {
            if (account.username().equals(fields.username()))
                occupancy.setUsernameOccupied(true);

            if (account.email().equals(fields.email()))
                occupancy.setEmailOccupied(true);

            if (occupancy.isUsernameOccupied() && occupancy.isEmailOccupied())
                break;
        }

        return occupancy;
    }

    private void checkAccountUniqueFieldsOccupancy(AccountUniqueFields fields){
        AccountUniqueFieldsOccupancy occupancy = getAccountUniqueFieldsOccupancy(fields);

        if (occupancy.isAnyOccupied()) {
            Map<String, String> occupiedFields = new HashMap<>();

            if (occupancy.isUsernameOccupied())
                occupiedFields.put("username", occupancy.getUsername());

            if (occupancy.isEmailOccupied())
                occupiedFields.put("email", occupancy.getEmail());

            throw new OccupiedValueException(occupiedFields);
        }
    }

    private void checkUpdatingConstraintsViolations(Account existingAccount, Account updatedAccount) {
        String constraintCheckingKey =
                !updatedAccount.getUsername().equals(existingAccount.getUsername()) +
                        String.valueOf(!updatedAccount.getEmail().equals(existingAccount.getEmail()));

        constraintsUpdatingCheckerMap.get(constraintCheckingKey).checkViolation(updatedAccount);
    }

    private void initializeConstraintsUpdatingCheckerMap() {
        constraintsUpdatingCheckerMap.put("truetrue", updatedAccount ->
                accountRepository
                        .findByUsernameOrEmail(updatedAccount.getUsername(), updatedAccount.getEmail())
                        .ifPresent(account -> {
                            Map<String, String> occupiedFields = new HashMap<>();

                            if (account.getUsername().equals(updatedAccount.getUsername()))
                                occupiedFields.put("username", account.getUsername());

                            if (account.getEmail().equals(updatedAccount.getEmail()))
                                occupiedFields.put("email", account.getEmail());

                            throw new OccupiedValueException(occupiedFields);
                        })
        );

        constraintsUpdatingCheckerMap.put("truefalse", updatedAccount ->
                accountRepository
                        .findByUsername(updatedAccount.getUsername())
                        .ifPresent(account -> {
                            Map<String, String> occupiedFields = new HashMap<>();

                            occupiedFields.put("username", account.getUsername());

                            throw new OccupiedValueException(occupiedFields);
                        })
        );

        constraintsUpdatingCheckerMap.put("falsetrue", updatedAccount ->
                accountRepository
                        .findByEmail(updatedAccount.getEmail())
                        .ifPresent(account -> {
                            Map<String, String> occupiedFields = new HashMap<>();

                            occupiedFields.put("email", account.getEmail());

                            throw new OccupiedValueException(occupiedFields);
                        })
        );

        constraintsUpdatingCheckerMap.put("falsefalse", updatedAccount -> {});
    }
}
