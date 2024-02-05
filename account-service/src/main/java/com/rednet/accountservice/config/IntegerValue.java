package com.rednet.accountservice.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= IntegerValueStringValidator.class)
public @interface IntegerValue {
    String message() default "Param should be an integer value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
