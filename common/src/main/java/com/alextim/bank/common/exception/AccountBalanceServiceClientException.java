package com.alextim.bank.common.exception;

public class AccountBalanceServiceClientException extends ServiceClientException {
    public AccountBalanceServiceClientException(String message, String code) {
        super(message, code);
    }
}