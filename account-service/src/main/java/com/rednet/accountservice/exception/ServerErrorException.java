package com.rednet.accountservice.exception;

public class ServerErrorException extends RuntimeException {
    private ServerErrorException(String message) {
        super(message);
    }
}
