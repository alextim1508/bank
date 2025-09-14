package com.alextim.bank.account.exception;

import com.alextim.bank.account.constant.ContactType;
import lombok.Getter;

@Getter
public class ContactAlreadyExistsException extends RuntimeException {

    private final ContactType contactType;
    private final String value;

    public ContactAlreadyExistsException(ContactType contactType, String value) {
        super(String.format("%s with value %s is already in use", contactType, value));
        this.contactType = contactType;
        this.value = value;
    }
}
