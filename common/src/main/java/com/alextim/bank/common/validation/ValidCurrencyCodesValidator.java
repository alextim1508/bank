package com.alextim.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

public class ValidCurrencyCodesValidator implements ConstraintValidator<ValidCurrencyCodes, List<String>> {

    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @Override
    public void initialize(ValidCurrencyCodes constraintAnnotation) {
    }


    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        for (String code : value) {
            if (code == null || code.isBlank()) {
                return false;
            }

            if (!CURRENCY_CODE_PATTERN.matcher(code).matches()) {
                return false;
            }
        }

        return true;
    }
}
