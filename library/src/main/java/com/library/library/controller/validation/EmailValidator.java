package com.library.library.controller.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class EmailValidator implements ConstraintValidator<EmailValid, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(email)) {
            log.warn("Email address is null");
            return true;
        }
        return email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");
    }
}
