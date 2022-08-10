package com.library.library.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UniqueNicknameAuthorValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueNicknameAuthor {

    String message() default "There is already author with this nickname!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
