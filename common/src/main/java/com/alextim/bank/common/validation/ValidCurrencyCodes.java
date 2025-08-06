package com.alextim.bank.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { CurrencyCodesValidator.class })
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrencyCodes {

    String message() default "{account.currency.codes}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
