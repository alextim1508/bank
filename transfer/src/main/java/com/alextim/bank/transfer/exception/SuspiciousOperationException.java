package com.alextim.bank.transfer.exception;

public class SuspiciousOperationException extends RuntimeException {
    public SuspiciousOperationException() {
        super("Suspicious operation");
    }
}
