package com.alextim.bank.cache.exception;

public class SuspiciousOperationException extends RuntimeException {
    public SuspiciousOperationException() {
        super("Suspicious operation");
    }
}
