package io.github.zaina01.request.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 注解保留到运行时
@Target(ElementType.TYPE)
public @interface Request {
    String url() default "";
    String value() default "";
    boolean openSession() default false;
    long connectTimeout() default 5;
    long timeout() default 10;
}
