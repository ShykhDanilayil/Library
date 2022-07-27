package com.library.library.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostalCodeValidator implements ConstraintValidator<PostalCodeValid, String> {
    @Override
    public boolean isValid(String postal, ConstraintValidatorContext constraintValidatorContext) {
        return postal != null && postal.matches("^\\d{5}$");
    }
}
