package com.rednet.accountservice.exception;

import java.util.Map;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Map<String, String> searchFields) {
        super("Account not found: " + searchFields);
    }
}
