package com.ccommit.fashionserver.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginCheck {
    public static enum UserType {
        USER, ADMIN, SELLER
    }

    UserType[] types() default {};
}
