package com.library.library.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {
    @Override
    public boolean isValid(String pass, ConstraintValidatorContext constraintValidatorContext) {
        return pass != null && pass.matches("(?=.*\\d)(?=.*[a-zA-Z]).{6,20}");
    }
}
