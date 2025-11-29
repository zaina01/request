package com.open.request;

import com.open.request.enums.RequestType;
import com.open.request.handler.ResultHandler;

import java.net.http.HttpClient;

public class HttpStatement {
    private String id;
    private Integer timeout;
    private RequestType requestType;
    private HttpClient httpClient;
    private String url;
    private boolean async;
    private Class<? extends ResultHandler<?>> resultHandlerClass;
    private boolean enableDefaultHeaders;
    HttpStatement(){}

    public static class Builder{
        private final HttpStatement httpStatement = new HttpStatement();
        public Builder(String id,RequestType requestType,HttpClient httpClient,String url){
            httpStatement.id = id;
            httpStatement.requestType = requestType;
            httpStatement.httpClient = httpClient;
            httpStatement.url = url;
        }

        public Builder timeout(Integer timeout) {
            httpStatement.timeout = timeout;
            return this;
        }
        public Builder async(boolean async) {
            httpStatement.async = async;
            return this;
        }
        public Builder resultHandler(Class<? extends ResultHandler<?>> resultHandlerClass){
            httpStatement.resultHandlerClass=resultHandlerClass;
            return this;
        }
        public Builder enableDefaultHeaders(boolean enableDefaultHeaders){
            httpStatement.enableDefaultHeaders = enableDefaultHeaders;
            return this;
        }
        public HttpStatement build(){
            return httpStatement;
        }
    }

    public String getId() {
        return id;
    }

    public Integer getTimeout() {
        return timeout;
    }
    public RequestType getRequestType() {
        return requestType;
    }
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Class<? extends ResultHandler<?>> getResultHandlerClass() {
        return resultHandlerClass;
    }

    public boolean getEnableDefaultHeaders() {
        return enableDefaultHeaders;
    }
    public boolean isAsync() {
        return async;
    }

    public String getUrl() {
        return url;
    }
}
