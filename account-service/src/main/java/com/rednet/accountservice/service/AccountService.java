package com.rednet.accountservice.service;

import com.rednet.accountservice.dto.AccountCreationBody;
import com.rednet.accountservice.entity.Account;

public interface AccountService {
    Account createAccount               (AccountCreationBody accountCreationBody);
    void    updateAccount               (Account updatedAccount);
    Account getAccountByID              (long ID);
    Account getAccountByUsernameOrEmail (String username, String email);
    Account getAccountByUsername        (String username);
    Account getAccountByEmail           (String email);
    boolean existsAccountByUsername     (String username);
    boolean existsAccountByEmail        (String email);
    void    deleteAccountByID           (long id);
}
