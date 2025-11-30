package com.open.request.proxy;

import com.open.request.*;
import com.open.request.annotation.RequestAsync;
import com.open.request.enums.RequestType;

import java.lang.reflect.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class RequestHttpMethod {

    private final MethodSignature methodSignature;

    private final HttpCommand command;

    public RequestHttpMethod(Class<?> httpInterface, Method method, Configuration config) {
        this.methodSignature = new MethodSignature(httpInterface, method);
        this.command = new HttpCommand(config, httpInterface, method);
    }

    public Object execute(Requests requests, Object[] args) {
        Object result;
        switch (command.getType()) {
            case Post -> result = requests.post(command.getName(), methodSignature, args);
            case Put -> result = requests.put(command.getName(), methodSignature, args);
            case Get -> result = requests.get(command.getName(), methodSignature, args);
            case Delete -> result = requests.delete(command.getName(), methodSignature, args);
            default -> throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    public static class HttpCommand {
        private final String name;
        private final RequestType type;

        public String getName() {
            return name;
        }

        public RequestType getType() {
            return type;
        }

        public HttpCommand(Configuration configuration, Class<?> httpInterface, Method method) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            HttpStatement httpStatement = resolveMappedStatement(httpInterface, methodName, declaringClass, configuration);
            if (httpStatement == null) {
                throw new RuntimeException(
                        "Invalid bound statement (not found): " + httpInterface.getName() + "." + methodName);
            } else {
                name = httpStatement.getId();
                type = httpStatement.getRequestType();
                if (type == RequestType.UNKNOWN) {
                    throw new RuntimeException("Unknown execution method for: " + name);
                }
            }
        }


        private HttpStatement resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass,
                                                     Configuration configuration) {
            String statementId = mapperInterface.getName() + "." + methodName;
            if (configuration.hasStatement(statementId)) {
                return configuration.getHttpStatement(statementId);
            }
            return null;
        }
    }

    public static class MethodSignature {
        //        private final boolean returnsMany;
        private final Type genericReturnType;
        private final Class<?> returnType;
        private final Class<?> actualType;
        private final ParamResolver paramResolver;
        private final Class<?>  mapperInterface;
        public MethodSignature(Class<?> mapperInterface, Method method) {
            this.mapperInterface = mapperInterface;
            Type type = method.getGenericReturnType();
            this.genericReturnType=type;
            RequestAsync annotation = method.getAnnotation(RequestAsync.class);
            if (annotation != null) {
                if (type instanceof ParameterizedType parameterizedType) {
                    Type rawType = parameterizedType.getRawType();
                    if (!((Class<?>) rawType).isAssignableFrom(CompletableFuture.class)) {
                        throw new RuntimeException("异步方法返回值只支持CompletableFuture类型");
                    }
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length != 1) {
                        throw new RuntimeException("异步方法返回值CompletableFuture类型的泛型必须为HttpResponse类型");
                    }
                    if (!(actualTypeArguments[0] instanceof ParameterizedType) || ((Class<?>) ((ParameterizedType) actualTypeArguments[0]).getRawType()).isAssignableFrom(HttpResponse.class)) {
                        throw new RuntimeException("异步方法返回值CompletableFuture类型的泛型HttpResponse类型也需要指定泛型");
                    }
                    if (((ParameterizedType) actualTypeArguments[0]).getActualTypeArguments()[0] instanceof Class<?> classType) {
                        this.returnType = (Class<?>) parameterizedType.getRawType();
                        this.actualType = classType;
                    }
                    throw new RuntimeException("异步方法返回值需要是CompletableFuture<HttpResponse<此处填写类型自定义类型处理器bodyHandler返回的泛型或填写默认String|byte[]>>类型");
                } else {
                    throw new RuntimeException("异步方法返回值只支持CompletableFuture类型");
                }
            } else {
                switch (type) {
                    case TypeVariable _ -> throw new RuntimeException("不支持泛型参数符号TypeVariable");
                    case GenericArrayType _ -> throw new RuntimeException("不支持泛型数组");
                    case ParameterizedType parameterizedType -> {
                        Type rawType = parameterizedType.getRawType();
                        this.returnType=(Class<?>) rawType;
                        this.actualType = (Class<?>) rawType;
                    }
                    case Class<?> clazz->{
                        this.returnType=clazz;
                        this.actualType = clazz;
                    }
                    default -> {
                        this.returnType = method.getReturnType();
                        this.actualType = method.getReturnType();
                    }
                }
            }
            this.paramResolver = new ParamResolver(method);
        }


        public ParamResolver getParamResolver() {
            return paramResolver;
        }

        public String convertArgsToHttpParam(Object[] args) {
            return paramResolver.urlBuild(args);
        }
        public String convertArgsToHttpPathVariable(String url,Object[] args) {
            return paramResolver.pathVariableBuild(url, args);
        }
        public HttpRequest.BodyPublisher convertArgsToHttpBodyPublisher(Object[] args) {
            return paramResolver.bodyBuild(args);
        }

        public HttpHeaders convertArgsToHttpHeaders(Object[] args) {
            return paramResolver.HeadersBuild(args);
        }

        public  Class<?> getReturnType() {
            return returnType;
        }

        public  Class<?> getActualType() {
            return actualType;
        }

        public Type getGenericReturnType() {
            return genericReturnType;
        }
        public Class<?> getMapperInterface() {
            return mapperInterface;
        }
    }
}
