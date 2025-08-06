package com.alextim.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class CurrencyCodeValidator implements ConstraintValidator<ValidCurrencyCode, String> {

    private static final Pattern ISO_4217_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && ISO_4217_PATTERN.matcher(value).matches();
    }
}
