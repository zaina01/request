package io.github.zaina01.request;

import com.open.request.annotation.*;
import io.github.zaina01.request.annotation.*;
import io.github.zaina01.request.json.Json;

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
    private final SortedMap<String, List<ParameterMapping>> names;
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
        SortedMap<String, List<ParameterMapping>> map = new TreeMap<>();
        int paramCount = parameterAnnotations.length;
        Parameter[] parameters = method.getParameters();
//        String[] parameterNames = new String[parameters.length];
//        for (int i = 0; i < paramCount; i++) {
//            parameterNames[i] = parameters[i].getName();
//        }
        String name;
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            Annotation[] parameterAnnotation = parameterAnnotations[paramIndex];
            if (parameterAnnotation.length == 0) {
                Parameter parameter = parameters[paramIndex];
                if (parameter.getType().isAssignableFrom(HttpHeaders.class)) {
                    hasHeadersAnnotation = true;
                    map.put("headers", new ArrayList<>(List.of(new ParameterMapping(paramIndex, "headers"))));
                } else {
                    name = getParameterName(parameter);
                    map.computeIfAbsent("params", _ -> new ArrayList<>()).add(new ParameterMapping(paramIndex, name));
                }
                continue;
            }
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RequestForm) {
                    hasRequestFormAnnotation = true;
                    map.put("body", new ArrayList<>(List.of(new ParameterMapping(paramIndex, "requestForm"))));
                    break;
                } else if (annotation instanceof RequestBody) {
                    hasRequestBodyAnnotation = true;
                    map.put("body", new ArrayList<>(List.of(new ParameterMapping(paramIndex, "requestBody"))));
                    break;
                } else if (annotation instanceof Headers) {
                    hasHeadersAnnotation = true;
                    map.put("headers", new ArrayList<>(List.of(new ParameterMapping(paramIndex, "headers"))));
                    break;
                } else if (annotation instanceof Param param) {
                    name = param.name();
                    if ("".equals(name)) name = param.value();
                    if ("".equals(name)) {
                        name = getParameterName(parameters[paramIndex]);
                    }
                    hasParamsAnnotation = true;
                    map.computeIfAbsent("params", _ -> new ArrayList<>()).add(new ParameterMapping(paramIndex, name));
                    break;
                } else if (annotation instanceof PathVariable pathVariable) {
                    hasPathVariableAnnotation = true;
                    name = pathVariable.name();
                    if ("".equals(name)) name = pathVariable.value();
                    if ("".equals(name)) {
                        name = getParameterName(parameters[paramIndex]);
                    }
                    map.computeIfAbsent("pathVariable", _ -> new ArrayList<>()).add(new ParameterMapping(paramIndex, name));
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

    public String pathVariableBuild(String url, Object[] args) {
        if (!hasPathVariableAnnotation) {
            return url;
        }
        List<ParameterMapping> parameterMappingList = names.getOrDefault("pathVariable", new ArrayList<>());
        for (ParameterMapping parameterMapping : parameterMappingList) {
            Object arg = args[parameterMapping.paramIndex()];
            String paramName = parameterMapping.paramName();
            Class<?> aClass = arg.getClass();
            if (WRAPPER_TYPES.contains(aClass) || aClass.isPrimitive() || aClass.isAssignableFrom(String.class)) {
                String value = String.valueOf(arg);
                url = url.replace("{" + paramName + "}", value);
            } else {
                throw new RuntimeException("pathVariable注解只支持基本类型、包装类、字符串类型");
            }
        }
        if (url.contains("{")|| url.contains("}")) {
            throw new IllegalArgumentException("pathVariable路径参数处理了"+parameterMappingList.size()+"个后 url参数中却还存在未被替换的花括号");
        }
        return url;
    }

    public String urlBuild(Object[] args) {
        if (!hasParamsAnnotation) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        List<ParameterMapping> params = names.getOrDefault("params", new ArrayList<>());
        params.forEach(parameterMapping -> {
            String paramName = parameterMapping.paramName();
            Object arg = args[parameterMapping.paramIndex()];
            Class<?> aClass = arg.getClass();
            if (aClass.isArray()) {
                throw new RuntimeException("Param注解不支持数组");
            }
            if (arg instanceof Collection) {
                throw new RuntimeException("Param注解不支持集合");
            }
            if (WRAPPER_TYPES.contains(aClass)) {
                sb.append(paramName);
                sb.append("=");
                sb.append(URLEncoder.encode(String.valueOf(arg), StandardCharsets.UTF_8));
                sb.append("&");
            } else if (aClass.isPrimitive() || aClass.isAssignableFrom(String.class)) {
                sb.append(paramName);
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

    public String getParameterName(Parameter parameter) {
        if (!parameter.isNamePresent())
            throw new IllegalArgumentException("获取方法参数名称失败，可能是因为未启用编译参数-parameters");
        return parameter.getName();
    }

    public HttpRequest.BodyPublisher bodyBuild(Object[] args) {
        List<ParameterMapping> parameterMappingList = names.get("body");
        if (parameterMappingList == null || parameterMappingList.isEmpty()) {
            return HttpRequest.BodyPublishers.noBody();
        }
        ParameterMapping first = parameterMappingList.getFirst();
        Integer paramedIndex = first.paramIndex();
        Object arg = args[paramedIndex];
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
            switch (arg) {
                case String body -> {
                    return HttpRequest.BodyPublishers.ofString(body);
                }
                case byte[] body -> {
                    return HttpRequest.BodyPublishers.ofByteArray(body);
                }
                case File file -> {
                    try {
                        return HttpRequest.BodyPublishers.ofFile(file.toPath());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                case Path path -> {
                    try {
                        return HttpRequest.BodyPublishers.ofFile(path);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                default -> {
                    String value = Json.toJSONString(arg);
                    return HttpRequest.BodyPublishers.ofString(String.valueOf(value));
                }
            }
        }
        return HttpRequest.BodyPublishers.noBody();
    }

    public HttpHeaders HeadersBuild(Object[] args) {
        if (!hasHeadersAnnotation) {
            return null;
        }
        List<ParameterMapping> parameterMappingList = names.get("headers");
        if (parameterMappingList == null || parameterMappingList.isEmpty()) {
            throw new IllegalArgumentException("hasHeadersAnnotation值为true,但从map中获取的parameterMappingList为 " + parameterMappingList);
        }
        ParameterMapping first = parameterMappingList.getFirst();
        Integer paramedIndex = first.paramIndex();
        Object arg = args[paramedIndex];
        if (arg instanceof HttpHeaders httpHeaders) {
            return httpHeaders;
        } else {
            throw new RuntimeException("Headers注解需要放在HttpHeaders对象上使用");
        }
    }
}
