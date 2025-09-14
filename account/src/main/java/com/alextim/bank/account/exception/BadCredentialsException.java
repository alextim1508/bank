package com.alextim.bank.account.exception;

public class BadCredentialsException extends RuntimeException {

    private String login;

    public BadCredentialsException(String login) {
        super(String.format("Bad password of account with login %s", login));
        this.login = login;
    }
}
