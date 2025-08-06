package com.alextim.bank.common.exception;

public abstract class ServiceClientException extends RuntimeException {

    public ServiceClientException(String message, String code) {
        super(String.format("Client service exception: %s, %s", message, code));
    }
}
