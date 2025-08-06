package com.alextim.bank.common.exception;

public class CurrencyFetchException extends ServiceClientException {
    public CurrencyFetchException (String message, String code) {
        super(message, code);
    }
}
