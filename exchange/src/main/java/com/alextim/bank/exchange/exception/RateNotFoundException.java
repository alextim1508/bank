package com.alextim.bank.exchange.exception;

public class RateNotFoundException extends RuntimeException {

    public RateNotFoundException(String currencyCode) {
        super(String.format("%s not found", currencyCode));
    }
}
