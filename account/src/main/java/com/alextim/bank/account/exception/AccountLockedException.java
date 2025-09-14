package com.alextim.bank.account.exception;

public class AccountLockedException extends RuntimeException {

    private final String login;
    private final String currency;

    public AccountLockedException(String login, String currency) {
        super(String.format("Balance %s of Account with login %s is locked", currency, login));
        this.login = login;
        this.currency = currency;
    }
}