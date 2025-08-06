package com.alextim.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NameFormatValidator implements ConstraintValidator<NameFormat, String> {

    @Override
    public void initialize(NameFormat constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String[] parts = value.trim().split("\\s+");
        return parts.length == 2;
    }
}