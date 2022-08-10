package com.library.library.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {
    @Override
    public boolean isValid(String pass, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(pass)) return true;
        return pass.matches("(?=.*\\d)(?=.*[a-zA-Z]).{6,20}");
    }
}
