package com.library.library.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String phoneNum, ConstraintValidatorContext constraintValidatorContext) {
        return phoneNum != null && phoneNum.matches("[0-9]{9}");
    }
}
