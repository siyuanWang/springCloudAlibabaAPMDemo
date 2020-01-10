package com.wsy.apm.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TransactionWithRemoteParent {
    String name() default "";

    String type() default co.elastic.apm.api.Transaction.TYPE_REQUEST;
}
