package com.library.library.controller.validation;

import com.library.library.service.LibraryService;
import com.library.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserService userService;
    private final LibraryService libraryService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(email)) {
            log.warn("Email address is null");
            return true;
        }
        return !userService.isEmailAlreadyInUse(email) && !libraryService.isEmailAlreadyInUse(email);
    }
}
