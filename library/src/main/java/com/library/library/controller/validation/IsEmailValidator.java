package com.library.library.controller.validation;

import com.library.library.service.UserService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class IsEmailValidator implements ConstraintValidator<IsEmail, String> {

    private final UserService userService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return userService.isEmailAlreadyInUse(email);
    }
}
