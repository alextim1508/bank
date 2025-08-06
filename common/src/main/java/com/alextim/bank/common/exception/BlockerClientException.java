package com.alextim.bank.common.exception;

public class BlockerClientException extends ServiceClientException {
    public BlockerClientException(String message, String code) {
        super(message, code);
    }
}