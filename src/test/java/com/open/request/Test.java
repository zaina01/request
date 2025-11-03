package com.open.request;

import com.open.request.annotation.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.Arrays;

public class Test {
    public static <T> T get(Class<T> clazz) {
        InvocationHandler invocationHandler= (proxy, method, args) -> {
            Class<?> returnType = method.getReturnType();
            System.out.println(returnType);
            Type genericReturnType = method.getGenericReturnType();
            System.out.println("TypeVariable"+(genericReturnType instanceof TypeVariable));
            System.out.println("GenericArrayType"+(genericReturnType instanceof GenericArrayType));
            System.out.println("ParameterizedType"+(genericReturnType instanceof ParameterizedType));
            System.out.println(Arrays.toString(Arrays.stream(((ParameterizedType) genericReturnType).getActualTypeArguments()).toArray()));
            if (method.isDefault()) {
                Class<?> declaringClass = method.getDeclaringClass();
                MethodHandles.Lookup lookup=MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
                MethodHandle methodHandle = lookup.findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
                return methodHandle.bindTo(proxy).invokeWithArguments(args);
            }
            System.out.println("参数===>"+Arrays.toString(args));
            Parameter[] parameters = method.getParameters();
            String meth = "";
            Object result = null;
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter=parameters[i];
                if(parameter.isAnnotationPresent(RequestForm.class)){
                    meth="form";
                    result=args[i];
                }
                if(parameter.isAnnotationPresent(RequestJson.class)){
                    meth="json";
                    result=args[i];
                }else {
                    meth="form";
                    result=args[i];
                }
            }
            boolean async = method.isAnnotationPresent(RequestAsync.class);
            if (method.isAnnotationPresent(Get.class)) {
                Get annotation = method.getAnnotation(Get.class);
                if(meth.equals("form")){
                    System.out.println("form==>"+result);
                }else if(meth.equals("json")){
                    System.out.println("json==>"+result);
                }
                String url = annotation.url();
                String value = annotation.value();
                if ("".equals(value)) {
                    value=url;
                }
                String s = HttpClientUtil.get(value);
                System.out.println(s);
                User user = new User();
                user.setName("2q12");
                return user;
            } else if (method.isAnnotationPresent(Post.class)) {
                Post annotation = method.getAnnotation(Post.class);
            }
//            Class<?> returnType = method.getReturnType();
            System.out.println(returnType);
            return returnType.getDeclaredConstructor().newInstance();
        };
        Object o = Proxy.newProxyInstance(Test.class.getClassLoader(), new Class[]{clazz}, invocationHandler);
        return (T) o;
    }
}
