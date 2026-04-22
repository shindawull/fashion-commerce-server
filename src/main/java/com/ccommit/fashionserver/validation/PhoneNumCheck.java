package com.ccommit.fashionserver.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumCheckValidator.class)
public @interface PhoneNumCheck {
    String message() default "휴대폰번호를 확인해주세요. (입력 예시:010-1234-1234) ";

    Class[] groups() default {};

    Class[] payload() default {};
}
