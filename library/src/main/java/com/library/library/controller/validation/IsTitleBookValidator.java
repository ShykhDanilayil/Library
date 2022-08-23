package com.library.library.controller.validation;

import com.library.library.service.BookService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class IsTitleBookValidator implements ConstraintValidator<IsTitleBook, String> {

    private final BookService service;

    @Override
    public boolean isValid(String title, ConstraintValidatorContext constraintValidatorContext) {
        return service.isExistBookTitle(title);
    }
}
