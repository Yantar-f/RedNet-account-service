package com.rednet.accountservice.service;

import com.rednet.accountservice.model.AccountCreationBody;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.model.AccountUniqueFields;
import com.rednet.accountservice.model.AccountUniqueFieldsOccupancy;

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
    AccountUniqueFieldsOccupancy getAccountUniqueFieldsOccupancy(AccountUniqueFields fields);
}
