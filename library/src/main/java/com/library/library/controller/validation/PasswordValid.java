package com.library.library.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValid {

    String message() default "Incorrect password format! It should contains at least one digit, one upper case or lower case letter and min length 6 symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
