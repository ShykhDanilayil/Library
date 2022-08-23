package com.library.library.controller.validation;

import com.library.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class UniqueNameLibraryValidator implements ConstraintValidator<UniqueNameLibrary, String> {

    private final LibraryService service;

    @Override
    public boolean isValid(String libName, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(libName)) {
            log.warn("Library name is null");
            return true;
        }
        return !service.isNameAlreadyInUse(libName);
    }
}
