package com.library.library.controller.validation;

import com.library.library.service.LibraryService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
@RequiredArgsConstructor
public class UniqueNameLibraryValidator implements ConstraintValidator<UniqueNameLibrary, String> {

    private final LibraryService service;
    @Override
    public boolean isValid(String libName, ConstraintValidatorContext constraintValidatorContext) {
        return !service.isNameAlreadyInUse(libName);
    }
}
