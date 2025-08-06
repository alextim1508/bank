package com.alextim.bank.account.exception;

import lombok.Getter;

@Getter
public class BalanceNotOpenedException extends RuntimeException {

    private final String login;
    private final String currency;

    public BalanceNotOpenedException(String login, String currency) {
        super(String.format("Balance %s of Account with login %s is not opened", currency, login));
        this.login = login;
        this.currency = currency;
    }
}
