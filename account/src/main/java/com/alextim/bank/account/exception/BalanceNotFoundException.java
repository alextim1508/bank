package com.alextim.bank.account.exception;

import lombok.Getter;

@Getter
public class BalanceNotFoundException extends RuntimeException {

    private final String login;
    private final String currency;

    public BalanceNotFoundException(String login, String currency) {
        super(String.format("Balance in the currency %s not found of account with login %s", currency, login));
        this.login = login;
        this.currency = currency;
    }
}