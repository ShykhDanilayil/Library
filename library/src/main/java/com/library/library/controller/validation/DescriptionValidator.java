package com.library.library.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DescriptionValidator implements ConstraintValidator<DescriptionValid, String> {

    @Override
    public void initialize(DescriptionValid contactNumber) {
    }

    @Override
    public boolean isValid(String bookDescription, ConstraintValidatorContext cxt) {
        return bookDescription.split(" ").length > 3;
    }
}