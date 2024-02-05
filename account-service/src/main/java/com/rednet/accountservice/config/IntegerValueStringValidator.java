package com.rednet.accountservice.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IntegerValueStringValidator implements ConstraintValidator<IntegerValue, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isEmpty())
            return false;

        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
