package com.alextim.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AgeValidator implements ConstraintValidator<AtLeast, LocalDate> {

    private int minYears;

    @Override
    public void initialize(AtLeast constraintAnnotation) {
        this.minYears = constraintAnnotation.years();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        return value.plusYears(minYears).isBefore(LocalDate.now());
    }
}
