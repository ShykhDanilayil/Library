package com.library.library.controller.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberValid, String> {
    @Override
    public boolean isValid(String phoneNum, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(phoneNum)) {
            log.warn("Phone number is null");
            return true;
        }
        return phoneNum.matches("^[0-9]{9,12}$");
    }
}
