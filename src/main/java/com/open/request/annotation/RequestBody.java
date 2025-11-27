package com.open.request.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 注解保留到运行时
@Target(ElementType.PARAMETER)
public @interface RequestBody {
}
