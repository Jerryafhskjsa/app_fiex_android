package com.black.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    /**
     * route path
     */
    String[] value();

    /**
     * before path
     */
    String beforePath() default "";

    /**
     * Fragment parent path
     */
    String fragmentParentPath() default "";

    /**
     * Fragment index in parent
     */
    int fragmentIndex() default -1;
}
