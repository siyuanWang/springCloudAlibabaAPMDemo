package com.wsy.apm.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transaction {
    String name();

    String type() default co.elastic.apm.api.Transaction.TYPE_REQUEST;
}
