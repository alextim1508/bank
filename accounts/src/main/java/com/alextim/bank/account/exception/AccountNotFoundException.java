package com.alextim.bank.account.exception;

import lombok.Getter;

@Getter
public class AccountNotFoundException extends RuntimeException {

    private final String login;
    private final Long id;

    public AccountNotFoundException(String login) {
        super(String.format("Account with login %s not found", login));
        this.login = login;
        this.id = null;
    }
}