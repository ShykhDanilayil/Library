package com.library.library.controller.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class DescriptionValidator implements ConstraintValidator<DescriptionValid, String> {

    @Override
    public void initialize(DescriptionValid descriptionValid) {
    }

    @Override
    public boolean isValid(String bookDescription, ConstraintValidatorContext cxt) {
        if (Objects.isNull(bookDescription)) {
            log.warn("Book description is null");
            return true;
        }
        return bookDescription.split(" ").length > 3;
    }
}