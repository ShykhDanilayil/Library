package com.library.library.controller.validation;

import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserService userService;
    private final LibraryService libraryService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return !userService.isEmailAlreadyInUse(email) && !libraryService.isEmailAlreadyInUse(email);
    }
}
