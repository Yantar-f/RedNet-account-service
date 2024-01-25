package com.rednet.accountservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String tokenName) {
        super("invalid " + tokenName);
    }
}
