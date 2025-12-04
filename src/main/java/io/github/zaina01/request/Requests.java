package io.github.zaina01.request;

import io.github.zaina01.request.enums.RequestType;
import io.github.zaina01.request.handler.ResultHandler;
import io.github.zaina01.request.json.Json;
import io.github.zaina01.request.proxy.RequestHttpMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Requests {
    private final Configuration configuration;

    public Requests(Configuration configuration) {
        this.configuration = configuration;
    }

    public Object post(String statement, RequestHttpMethod.MethodSignature methodSignature, Object[] args) {
        return execute(statement, methodSignature, args, RequestType.Post);
    }

    public Object put(String statement, RequestHttpMethod.MethodSignature methodSignature, Object[] args) {
        return execute(statement, methodSignature, args, RequestType.Put);
    }

    public Object get(String statement, RequestHttpMethod.MethodSignature methodSignature, Object[] args) {
        return execute(statement, methodSignature, args, RequestType.Get);
    }

    public Object delete(String statement, RequestHttpMethod.MethodSignature methodSignature, Object[] args) {
        return execute(statement, methodSignature, args, RequestType.Delete);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Object execute(String statement, RequestHttpMethod.MethodSignature methodSignature, Object[] args, RequestType requestType) {
        HttpStatement httpStatement = configuration.getHttpStatement(statement);
        HttpRequest httpRequest = buildRequest(requestType, httpStatement.getUrl(), methodSignature, args,httpStatement.getEnableDefaultHeaders(),httpStatement.getTimeout());
        ResultHandler<?> resultHandler = configuration.getResultHandler(httpStatement.getResultHandlerClass());
        if (httpStatement.isAsync()){
            HttpResponse.BodyHandler<?> bodyHandler = buildBodyHandler((Class<?>)methodSignature.getActualType());
            return Executor.executeAsync(httpStatement,httpRequest,bodyHandler);
        }
        HttpResponse<?> execute = Executor.execute(httpStatement, httpRequest, resultHandler.bodyHandler());
        Object handle = resultHandler.executeHandle(execute);
        return resultResolver(handle, methodSignature);
    }

    public Object resultResolver(Object result, RequestHttpMethod.MethodSignature methodSignature) {
        if (result == null) {
            return null;
        }
        Class<?> resultClass = result.getClass();
        Class<?> returnType = methodSignature.getReturnType();
        Type genericReturnType = methodSignature.getGenericReturnType();
        if (resultClass.isAssignableFrom(returnType)){
            return result;
        }
        if (result instanceof byte[] bytes){
            result=new String(bytes, StandardCharsets.UTF_8);
        }
        if (result instanceof String string){
            if (returnType.isAssignableFrom(String.class)){
                return result;
            }
            switch (genericReturnType){
                case ParameterizedType parameterizedType-> {
                    return Json.parseObject(string,parameterizedType);
                }
                case Class<?> clazz-> {
                    return Json.parseObject(string,clazz);
                }
                default -> {
                    return Json.parseObject(string,returnType);
                }
            }
        }
        throw new RuntimeException("转换结果失败 需要的类型" + returnType + ",提供的result类型" + result.getClass());
    }

    public HttpRequest buildRequest(RequestType requestType, String url, RequestHttpMethod.MethodSignature methodSignature, Object[] args,boolean enableDefaultHeaders, long timeout) {
        url = methodSignature.convertArgsToHttpPathVariable(url, args);
        String param = methodSignature.convertArgsToHttpParam(args);
        HttpHeaders httpHeaders = methodSignature.convertArgsToHttpHeaders(args);
        HttpHeaders httpDefaultHeaders = configuration.getHttpDefaultHeaders(methodSignature.getMapperInterface());
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.timeout(Duration.ofSeconds(timeout));
        requestBuilder.uri(URI.create("".equals(url) ? url : url + param));
        if (enableDefaultHeaders){
            if (httpDefaultHeaders!=null) httpDefaultHeaders.build(requestBuilder);
        }
        if (httpHeaders != null) httpHeaders.build(requestBuilder);
        switch (requestType) {
            case Post -> {
                HttpRequest.BodyPublisher bodyPublisher = methodSignature.convertArgsToHttpBodyPublisher(args);
                return requestBuilder.POST(bodyPublisher).build();
            }
            case Put -> {
                HttpRequest.BodyPublisher bodyPublisher = methodSignature.convertArgsToHttpBodyPublisher(args);
                return requestBuilder.PUT(bodyPublisher).build();
            }
            case Get -> {
                return requestBuilder.GET().build();
            }
            case Delete -> {
                return requestBuilder.DELETE().build();
            }
            default -> throw new RuntimeException("Unsupported request type");
        }
    }

    public <T> HttpResponse.BodyHandler<T> buildBodyHandler(Class<T> actualType) {
        if (actualType.isAssignableFrom(byte[].class)) {
            return (HttpResponse.BodyHandler<T>) HttpResponse.BodyHandlers.ofByteArray();

        }
        return (HttpResponse.BodyHandler<T>) HttpResponse.BodyHandlers.ofString();
    }
}
