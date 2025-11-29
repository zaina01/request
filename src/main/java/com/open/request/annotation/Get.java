package com.open.request.annotation;

import com.open.request.enums.RequestType;
import com.open.request.handler.DefaultResultHandler;
import com.open.request.handler.ResultHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 注解保留到运行时
@Target(ElementType.METHOD)
public @interface Get {
    String url() default "";
    String value() default "";
    Class<? extends ResultHandler<?>>  handler() default DefaultResultHandler.class;
    boolean enableDefaultHeaders() default true;
}
