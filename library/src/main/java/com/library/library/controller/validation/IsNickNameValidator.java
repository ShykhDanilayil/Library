package com.library.library.controller.validation;

import com.library.library.service.AuthorService;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
@RequiredArgsConstructor
public class IsNickNameValidator implements ConstraintValidator<IsNickName, String> {

    private final AuthorService service;
    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext constraintValidatorContext) {
        return service.isNicknameAlreadyInUse(nickname);
    }
}
