package com.library.library.controller.validation;

import com.library.library.service.LibraryService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class IsNameLibraryValidator implements ConstraintValidator<IsNameLibrary, String> {

    private final LibraryService service;

    @Override
    public boolean isValid(String libraryName, ConstraintValidatorContext constraintValidatorContext) {
        return service.isNameAlreadyInUse(libraryName);
    }
}
