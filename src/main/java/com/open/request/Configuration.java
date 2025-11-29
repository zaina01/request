package com.open.request;

import com.open.request.handler.ResultHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration {

    protected final RequestHttpRegistry requestHttpRegistry = new RequestHttpRegistry(this);

    protected final Map<String, HttpStatement> httpStatements=new ConcurrentHashMap<>();

    protected final Map<String, ResultHandler<?>>  resultHandlers=new ConcurrentHashMap<>();
    protected final Map<Class<?>, HttpHeaders> httpDefaultHeaders = new ConcurrentHashMap<>();
    public void addHttpStatement(HttpStatement httpStatement) {
        httpStatements.put(httpStatement.getId(), httpStatement);
    }
    public boolean hasStatement(String statementId) {
        return httpStatements.containsKey(statementId);
    }
    public HttpStatement getHttpStatement(String id) {
        return httpStatements.get(id);
    }


    public <T> void addRequest(Class<T> type) {
        requestHttpRegistry.addRequest(type);
    }
    public <T> T getRequest(Class<T> type) {
        return getRequest(type,new Requests(this));
    }
    public <T> T getRequest(Class<T> type, Requests requests) {
        return requestHttpRegistry.getRequest(type, requests);
    }
    public void addHttpDefaultHeader(Class<?> type, HttpHeaders httpDefaultHeader) {
        httpDefaultHeaders.put(type, httpDefaultHeader);
    }
    public boolean hasHttpDefaultHeaders(Class<?> type) {
        return httpDefaultHeaders.containsKey(type);
    }
    public HttpHeaders getHttpDefaultHeaders(Class<?> type) {
        return httpDefaultHeaders.get(type);
    }
    public ResultHandler<?> getResultHandler(Class<? extends ResultHandler<?>> resultHandlerClass) {
        return resultHandlers.computeIfAbsent(resultHandlerClass.getName(), _ -> {
            try {
                return resultHandlerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
