package com.rednet.accountservice.service.impl;

import com.rednet.accountservice.dto.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.repository.AccountRepository;
import com.rednet.accountservice.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
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

        constraintsUpdatingCheckerMap.put("truetrue", updatedAccount ->
            accountRepository
                .findByUsernameOrEmail(updatedAccount.getUsername(), updatedAccount.getEmail())
                .ifPresent(account -> {
                    Map<String, String> occupiedFields = new HashMap<>();

                    if (account.getUsername().equals(updatedAccount.getUsername())) {
                        occupiedFields.put("username", account.getUsername());
                    }

                    if (account.getEmail().equals(updatedAccount.getEmail())) {
                        occupiedFields.put("email", account.getEmail());
                    }

                    throw new OccupiedValueException(occupiedFields);
                })
        );

        constraintsUpdatingCheckerMap.put("truefalse", updatedAccount ->
            accountRepository.findByUsername(updatedAccount.getUsername()).ifPresent(account -> {
                Map<String, String> occupiedFields = new HashMap<>();
                occupiedFields.put("username", account.getUsername());

                throw new OccupiedValueException(occupiedFields);
            })
        );

        constraintsUpdatingCheckerMap.put("falsetrue", updatedAccount ->
            accountRepository.findByEmail(updatedAccount.getEmail()).ifPresent(account -> {
                Map<String, String> occupiedFields = new HashMap<>();
                occupiedFields.put("email", account.getEmail());

                throw new OccupiedValueException(occupiedFields);
            })
        );

        constraintsUpdatingCheckerMap.put("falsefalse", updatedAccount -> {});
    }

    @Override
    public Account createAccount(AccountCreationBody accountCreationBody) {
        accountRepository.findByUsernameOrEmail(accountCreationBody.username(), accountCreationBody.email())
            .ifPresent(existingAccount -> {
                Map<String, String> occupiedFields = new HashMap<>();

                if (existingAccount.getUsername().equals(accountCreationBody.username())) {
                    occupiedFields.put("username", existingAccount.getUsername());
                }

                if (existingAccount.getEmail().equals(accountCreationBody.email())) {
                    occupiedFields.put("email", existingAccount.getEmail());
                }

                throw new OccupiedValueException(occupiedFields);
            });
        Account account = new Account(
            accountCreationBody.username(),
            accountCreationBody.email(),
            accountCreationBody.password(),
            accountCreationBody.secretWord(),
            Arrays.stream(accountCreationBody.roles()).map(Role::new).toList()
        );

        return accountRepository.save(account);
    }

    @Override
    public void updateAccount(Account updatedAccount) {
        Account existingAccount = accountRepository.findById(updatedAccount.getID()).orElseThrow(() -> {
            Map<String, String> searchFields = new HashMap<>();
            searchFields.put("ID", String.valueOf(updatedAccount.getID()));

            return new AccountNotFoundException(searchFields);
        });

        String constraintCheckingKey =
            !updatedAccount.getUsername().equals(existingAccount.getUsername()) +
            String.valueOf(!updatedAccount.getEmail().equals(existingAccount.getEmail()));

        constraintsUpdatingCheckerMap.get(constraintCheckingKey).checkViolation(updatedAccount);

        existingAccount.setUsername(updatedAccount.getUsername());
        existingAccount.setEmail(updatedAccount.getEmail());
        existingAccount.setPassword(updatedAccount.getPassword());
        existingAccount.setSecretWord(updatedAccount.getSecretWord());
        existingAccount.setRoles(updatedAccount.getRoles());

        accountRepository.save(existingAccount);
    }

    @Override
    public Account getAccountByID(long ID) {
        return accountRepository.findById(ID).orElseThrow(() -> {
            Map<String, String> searchFields = new HashMap<>();
            searchFields.put("ID", String.valueOf(ID));

            return new AccountNotFoundException(searchFields);
        });
    }

    @Override
    public Account getAccountByUsernameOrEmail(String username, String email) {
        return accountRepository.findByUsernameOrEmail(username, email).orElseThrow(() -> {
            Map<String, String> searchFields = new HashMap<>();

            searchFields.put("username", username);
            searchFields.put("email", email);

            return new AccountNotFoundException(searchFields);
        });
    }

    @Override
    public Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username).orElseThrow(() -> {
            Map<String, String> searchFields = new HashMap<>();
            searchFields.put("username", username);

            return new AccountNotFoundException(searchFields);
        });
    }

    @Override
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(() -> {
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
        Account account = accountRepository.findById(ID).orElseThrow(() -> {
            Map<String, String> searchFields = new HashMap<>();
            searchFields.put("ID", String.valueOf(ID));

            return new AccountNotFoundException(searchFields);
        });

        accountRepository.delete(account);
    }
}
