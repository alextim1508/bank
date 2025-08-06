package com.alextim.bank.common.exception;

public class AccountClientException extends ServiceClientException {
    public AccountClientException(String message, String code) {
        super(message, code);
    }
}
