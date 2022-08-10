package com.library.library.controller.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class PostalCodeValidator implements ConstraintValidator<PostalCodeValid, String> {
    @Override
    public boolean isValid(String postal, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(postal)) {
            log.warn("Postal code is null");
            return true;
        }
        return postal.matches("^\\d{5}$");
    }
}
