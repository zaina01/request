package com.open.request;

import com.open.request.annotation.*;
import com.open.request.json.Json;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ParamResolver {
    private final SortedMap<Integer, String> names;
    private boolean hasParamsAnnotation;
    private boolean hasRequestFormAnnotation;
    private boolean hasRequestBodyAnnotation;
    private boolean hasHeadersAnnotation;
    private boolean hasPathVariableAnnotation;
    private static final Set<Class<?>> WRAPPER_TYPES = Set.of(
            Boolean.class, Byte.class, Character.class, Double.class,
            Float.class, Integer.class, Long.class, Short.class
    );

    public ParamResolver(Method method) {
//        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        SortedMap<Integer, String> map = new TreeMap<>();
        int paramCount = parameterAnnotations.length;
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < paramCount; i++) {
            parameterNames[i]=parameters[i].getName();
        }
        String name;
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            Annotation[] parameterAnnotation = parameterAnnotations[paramIndex];
            if (parameterAnnotation.length==0){
                Parameter parameter = parameters[paramIndex];
                if (!parameter.isNamePresent()) throw new  IllegalArgumentException("获取方法参数名称失败，可能是因为未启用编译参数-parameters");
                hasParamsAnnotation = true;
                name=parameterNames[paramIndex];
                map.put(paramIndex,name);
                continue;
            }
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RequestForm) {
                    hasRequestFormAnnotation = true;
                    map.put(-1, String.valueOf(paramIndex));
                    break;
                } else if (annotation instanceof RequestBody) {
                    hasRequestBodyAnnotation = true;
                    map.put(-1, String.valueOf(paramIndex));
                    break;
                } else if (annotation instanceof Headers) {
                    hasHeadersAnnotation=true;
                    map.put(-2, String.valueOf(paramIndex));
                    break;
                } else if (annotation instanceof Param param) {
                    name = param.name();
                    if ("".equals(name)) name=param.value();
                    if ("".equals(name)) {
                        name=parameterNames[paramIndex];
                    }
                    hasParamsAnnotation = true;
                    map.put(paramIndex,name);
                    break;
                } else if (annotation instanceof PathVariable) {
                    hasPathVariableAnnotation=true;
                    //todo 等待实现
                }
            }
        }
        names = Collections.unmodifiableSortedMap(map);
    }
    public String[] getNames() {
        return names.values().toArray(new String[0]);
    }

    public Integer[] getParamIndexList() {
        return names.keySet().toArray(new Integer[0]);
    }

    public String urlBuild(Object[] args) {
        if (!hasParamsAnnotation) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        Integer[] paramIndexList = this.getParamIndexList();
        Arrays.stream(paramIndexList).filter(paramIndex -> paramIndex > -1).forEach(paramIndex -> {
            String s = names.get(paramIndex);
            Object arg = args[paramIndex];
            Class<?> aClass = arg.getClass();
            if (aClass.isArray()) {
                throw new RuntimeException("Param注解不支持数组");
            }
            if (arg instanceof Collection) {
                throw new RuntimeException("Param注解不支持集合");
            }
            if (WRAPPER_TYPES.contains(aClass)) {
                sb.append(s);
                sb.append("=");
                sb.append(URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
                sb.append("&");
            } else if (aClass.isPrimitive() || aClass.isAssignableFrom(String.class)) {
                sb.append(s);
                sb.append("=");
                sb.append(URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
                sb.append("&");
            } else {
                try {
                    Field[] declaredFields = aClass.getDeclaredFields();
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(aClass, MethodHandles.lookup());
                    for (Field field : declaredFields) {
                        MethodHandle methodHandle = lookup.unreflectGetter(field).bindTo(arg);
                        Object invoke = methodHandle.invoke();
                        if (invoke != null) {
                            sb.append(field.getName());
                            sb.append("=");
                            sb.append(URLEncoder.encode(String.valueOf(invoke), StandardCharsets.UTF_8));
                            sb.append("&");
                        }
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
        if (sb.charAt(sb.length() - 1) == '&') {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    public HttpRequest.BodyPublisher bodyBuild(Object[] args) {
        String s = names.get(-1);
        int paramIndex = Integer.parseInt(s);
        Object arg = args[paramIndex];
        Class<?> aClass = arg.getClass();
        if (hasRequestFormAnnotation) {
            StringBuilder sb = new StringBuilder();
            try {
                Field[] declaredFields = aClass.getDeclaredFields();
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(aClass, MethodHandles.lookup());
                for (Field field : declaredFields) {
                    MethodHandle methodHandle = lookup.unreflectGetter(field).bindTo(arg);
                    Object invoke = methodHandle.invoke();
                    if (invoke != null) {
                        sb.append(field.getName());
                        sb.append("=");
                        sb.append(URLEncoder.encode(String.valueOf(invoke), StandardCharsets.UTF_8));
                        sb.append("&");
                    }
                }
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable.getMessage(), throwable);
            }
            sb.delete(sb.length() - 1, sb.length());
            return HttpRequest.BodyPublishers.ofString(sb.toString());
        } else if (hasRequestBodyAnnotation) {
            if (arg instanceof String body) {
                return HttpRequest.BodyPublishers.ofString(body);
            } else if (arg instanceof byte[] body) {
                return HttpRequest.BodyPublishers.ofByteArray(body);
            } else if (arg instanceof File file){
                try {
                    return HttpRequest.BodyPublishers.ofFile(file.toPath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }else if (arg instanceof Path path){
                try {
                    return HttpRequest.BodyPublishers.ofFile(path);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                String value = Json.toJSONString(arg);
                return HttpRequest.BodyPublishers.ofString(String.valueOf(value));
            }
        }
        return HttpRequest.BodyPublishers.noBody();
    }
    public HttpHeaders HeadersBuild(Object[] args) {
        if (!hasHeadersAnnotation) {
            return null;
        }
        String s = names.get(-2);
        int paramIndex = Integer.parseInt(s);
        Object arg = args[paramIndex];
        if (arg instanceof HttpHeaders httpHeaders){
            return httpHeaders;
        }else {
            throw new RuntimeException("Headers注解需要放在HttpHeaders对象上使用");
        }
    }
}
