package com.rednet.accountservice.controller;

import com.rednet.accountservice.model.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.model.AccountUniqueFields;
import com.rednet.accountservice.model.AccountUniqueFieldsOccupancy;
import com.rednet.accountservice.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountCreationBody creationBody) {
        return ResponseEntity.ok(accountService.createAccount(creationBody));
    }

    @PutMapping
    public ResponseEntity<Void> updateAccount(@Valid @RequestBody Account account) {
        accountService.updateAccount(account);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/by-id")
    public ResponseEntity<Account> getAccountByID(
            @NotBlank(message = "ID min length is 1")
            @Digits(fraction = 0, integer = 0, message = "ID is number")
            @RequestParam(name = "id")
            Long ID) {
        return ResponseEntity.ok(accountService.getAccountByID(ID));
    }

    @DeleteMapping(path = "/by-id")
    public ResponseEntity<Void> deleteAccountByID(
            @NotBlank(message = "ID min length is 1")
            @Digits(fraction = 0, integer = 0, message = "ID is number")
            @RequestParam(name = "id")
            Long ID) {
        accountService.deleteAccountByID(ID);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/by-username",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> getAccountByUsername(
            @NotBlank(message = "username min length is 1") @RequestParam String username) {
        return ResponseEntity.ok(accountService.getAccountByUsername(username));
    }

    @RequestMapping(path = "/by-username",
                    method = RequestMethod.HEAD)
    public ResponseEntity<Void> existsAccountByUsername(
            @NotBlank(message = "username min length is 1") @RequestParam String username) {
        if (accountService.existsAccountByUsername(username)) {
            return ResponseEntity.ok().build();
        } else {
            Map<String, String> searchFields = new HashMap<>();

            searchFields.put("username", username);

            throw new AccountNotFoundException(searchFields);
        }
    }

    @GetMapping(path = "/by-email",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> getAccountByEmail(
            @NotBlank(message = "email min length is 1") @RequestParam String email) {
        return ResponseEntity.ok(accountService.getAccountByEmail(email));
    }

    @RequestMapping(path = "/by-email",
                    method = RequestMethod.HEAD)
    public ResponseEntity<Void> existsAccountByEmail(
            @NotBlank(message = "email min length is 1") @RequestParam String email) {
        if (accountService.existsAccountByEmail(email)) {
            return ResponseEntity.ok().build();
        } else {
            Map<String, String> searchFields = new HashMap<>();

            searchFields.put("email", email);

            throw new AccountNotFoundException(searchFields);
        }
    }

    @GetMapping(path = "/by-username-or-email",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> getAccountByUsernameOrEmail(
            @NotBlank(message = "username min length is 1") @RequestParam String username,
            @NotBlank(message = "email min length is 1") @RequestParam String email) {
        return ResponseEntity.ok(accountService.getAccountByUsernameOrEmail(username,email));
    }

    @GetMapping(path = "/unique-fields-occupancy",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountUniqueFieldsOccupancy> getAccountUniqueFieldsOccupancy(
            @NotBlank(message = "username min length is 1") @RequestParam String username,
            @NotBlank(message = "email min length is 1") @RequestParam String email) {
        AccountUniqueFields fields = new AccountUniqueFields(username, email);

        return ResponseEntity.ok(accountService.getAccountUniqueFieldsOccupancy(fields));
    }
}
