package com.open.request.proxy;

import com.open.request.Configuration;
import com.open.request.HttpStatement;
import com.open.request.ParamResolver;
import com.open.request.Requests;
import com.open.request.annotation.RequestAsync;
import com.open.request.enums.RequestType;
import com.open.request.utils.HeadersUtil;

import java.lang.reflect.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RequestHttpMethod {

    private final MethodSignature methodSignature;

    private final HttpCommand  command;

    public RequestHttpMethod(Class<?> httpInterface, Method method, Configuration config) {
        this.methodSignature = new MethodSignature(httpInterface, method);
        this.command=new HttpCommand(config,httpInterface,method);
    }

    public Object execute(Requests requests, Object[] args) {
        Object result;
        switch(command.getType()){
            case Post -> result=requests.post(command.getName(),methodSignature,args);
            case Put -> result=requests.put(command.getName(),methodSignature,args);
            case Get -> result=requests.get(command.getName(),methodSignature,args);
            case Delete -> result=requests.delete(command.getName(),methodSignature,args);
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
        private final boolean returnsMany;
        private final Class<?> returnType;
        private final Class<?> actualType;
        private final ParamResolver paramResolver;

        public MethodSignature(Class<?> mapperInterface, Method method) {
            Type type = method.getGenericReturnType();
            RequestAsync annotation = method.getAnnotation(RequestAsync.class);
            if (type instanceof TypeVariable) {
                throw new RuntimeException("不支持泛型参数符号");
            } else if (type instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (annotation!=null && !((Class<?>) rawType).isAssignableFrom(CompletableFuture.class)){
                    throw  new RuntimeException("异步方法返回值只支持CompletableFuture类型");
                }
                if (annotation==null && !((Class<?>) rawType).isAssignableFrom(List.class)) {
                    throw new RuntimeException("返回值只支持List类型和对象类型");
                }
                Type[] actualTypeArguments=null;
                if (((Class<?>) rawType).isAssignableFrom(CompletableFuture.class)){
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (actualTypeArgument instanceof ParameterizedType typeArgument) {
                        if (!((Class<?>)typeArgument.getRawType()).isAssignableFrom(HttpResponse.class)) {
                            throw new RuntimeException("CompletableFuture类型的泛型必须是HttpResponse");
                        }
                        actualTypeArguments = typeArgument.getActualTypeArguments();
                    }else {
                        throw new RuntimeException("CompletableFuture类型的泛型必须是HttpResponse");
                    }

                }else {
                    actualTypeArguments = parameterizedType.getActualTypeArguments();
                }

                if (actualTypeArguments.length != 1) {
                    throw new RuntimeException("获取到的泛型参数不为1个请检查是否是List类型");
                }
                this.returnType = (Class<?>) parameterizedType.getRawType();
                this.actualType = (Class<?>) actualTypeArguments[0];
            } else if (type instanceof GenericArrayType) {
                throw new RuntimeException("不支持泛型数组");
            } else if (type instanceof Class<?>) {
                this.returnType = (Class<?>) type;
                this.actualType = (Class<?>) type;
            } else {
                this.returnType = method.getReturnType();
                this.actualType = method.getReturnType();
            }
            this.returnsMany = List.class.isAssignableFrom(this.returnType);
            this.paramResolver = new ParamResolver(method);
        }

        public boolean isReturnsMany() {
            return returnsMany;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public Class<?> getActualType() {
            return actualType;
        }

        public ParamResolver getParamResolver() {
            return paramResolver;
        }

        public String convertArgsToHttpParam(Object[] args){
            return paramResolver.urlBuild(args);
        }

        public HttpRequest.BodyPublisher convertArgsToHttpBodyPublisher(Object[] args){
            return paramResolver.bodyBuild(args);
        }
        public HeadersUtil convertArgsToHttpHeaders(Object[] args){
            return paramResolver.HeadersBuild(args);
        }
    }
}
