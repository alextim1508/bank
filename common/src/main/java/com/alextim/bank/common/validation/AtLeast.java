package com.alextim.bank.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeast {
    int years() default 18;

    String message() default "Must be over {years} years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}